/*
 * Copyright (C) 2019. OpenLattice, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 *
 */

package com.openlattice.tasks

import com.google.common.base.Stopwatch
import com.hazelcast.core.HazelcastInstance
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import java.util.concurrent.TimeUnit

const val HAZELCAST_SCHEDULED_TASKS_EXECUTOR_NAME = "hazelcast_scheduled_tasks"
private val logger = LoggerFactory.getLogger(TaskService::class.java)

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class TaskService(
        context: ApplicationContext,
        dependenciesMap: Map<Class<*>, HazelcastTaskDependencies>,
        tasks: Set<HazelcastFixedRateTask<*>>,
        private val initializers: Set<HazelcastInitializationTask<*>>,
        hazelcast: HazelcastInstance
) {
    private val executor = hazelcast.getScheduledExecutorService(HAZELCAST_SCHEDULED_TASKS_EXECUTOR_NAME)

    init {
//        TaskSchedulerGlobalContext.setApplicationContext(context)
        dependencies = dependenciesMap
        //We sort the initializers before running to make sure that initialization task are run in the correct order.
        initializers
                .toSortedSet(TaskComparator(initializers))
                .forEach { initializer ->
                    val sw = Stopwatch.createStarted()
                    val f = executor.schedule(initializer, initializer.getInitialDelay(), initializer.getTimeUnit())

                    logger.info(
                            "Scheduled initializer {} with initialDelay {} and period {} in time unit",
                            initializer.name,
                            initializer.getInitialDelay(),
                            initializer.getTimeUnit()
                    )

                    f.get()

                    logger.info(
                            "Coompleted execution of initializer {} in {} millis",
                            initializer.name,
                            sw.elapsed(TimeUnit.MILLISECONDS)
                    )
                }
    }

    private val taskFutures = tasks.map { task ->
        executor.scheduleAtFixedRate(task, task.getInitialDelay(), task.getPeriod(), task.getTimeUnit())
        logger.info(
                "Scheduled task {} with initialDelay {} and period {} in time unit",
                task.name,
                task.getInitialDelay(),
                task.getPeriod(),
                task.getTimeUnit()
        )
    }

    companion object {
        private lateinit var dependencies: Map<Class<*>, HazelcastTaskDependencies>
    }

    interface HazelcastDependencyAwareTask<T : HazelcastTaskDependencies> {
        fun getDependenciesClass(): Class<out T>

        @JvmDefault
        fun getDependency(): T {
            return (dependencies[getDependenciesClass()] ?: throw IllegalStateException(
                    "Unable to find dependency ${getDependenciesClass().canonicalName}"
            )) as T
        }
    }

    class TaskComparator(
            initializers: Set<HazelcastInitializationTask<*>>
    ) : Comparator<HazelcastInitializationTask<*>> {
        private val initMap = initializers.map { (it.javaClass as Class<*>) to it }.toMap()
        private val ancestorMap = mutableMapOf<Class<*>, MutableSet<Class<*>>>()
        override fun compare(a: HazelcastInitializationTask<*>, b: HazelcastInitializationTask<*>): Int {
            val ancestorsOfA = ancestorMap.getOrPut(a.javaClass) {
                a.after().flatMap { clazz: Class<*> ->
                    ancestorMap.getOrPut(clazz) { expandAncestors(clazz).toMutableSet() }
                }.toMutableSet()
            }

            val ancestorsOfB = ancestorMap.getOrPut(b.javaClass) {
                b.after().flatMap { clazz: Class<*> ->
                    ancestorMap.getOrPut(clazz) { expandAncestors(clazz).toMutableSet() }
                }.toMutableSet()
            }

            return if (ancestorsOfA.contains(a.javaClass) && ancestorsOfB.contains(b.javaClass)) {
                logger.error(
                        "Detected cycle in task graph. ${a.javaClass.canonicalName} must happen after ${b.javaClass.canonicalName} and vice-versa."
                )
                throw IllegalStateException(
                        "Detected cycle in task graph. ${a.javaClass.canonicalName} must happen after ${b.javaClass.canonicalName} and vice-versa."
                )
            } else if (ancestorsOfA.contains(b.javaClass)) {
                //1 argument is greater than second argument as it must be initialized after
                1
            } else if (ancestorsOfB.contains(a.javaClass)) {
                //1 argument is less than second argument as it must be initialized before
                -1
            } else {
                //Order of these two tasks does not matter
                0
            }
        }


        private fun expandAncestors(initial: Class<*>): Set<Class<*>> {
            val ancestors = mutableSetOf<Class<*>>()
            ancestors += initial
            var current = setOf(initial)
            while (current.isNotEmpty()) {
                current = current.flatMap {
                    initMap.getValue(it).after()
                }.toSet()
                ancestors += current
            }
            return ancestors
        }
    }
}
