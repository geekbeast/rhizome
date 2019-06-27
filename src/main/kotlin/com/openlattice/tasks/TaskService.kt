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
import com.hazelcast.scheduledexecutor.IScheduledFuture
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler
import com.kryptnostic.rhizome.startup.Requirement
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

const val SUBMITTED_TASKS = "_rhizome:submitted-tasks"
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
    companion object {
        private val latch = CountDownLatch(1)
        private val startupLatch = CountDownLatch(1)
        private val initializersCompletedRequirements = InitializersCompletedRequirements()
        private lateinit var dependencies: Map<Class<*>, HazelcastTaskDependencies>
    }

    private val executor = hazelcast.getScheduledExecutorService(HAZELCAST_SCHEDULED_TASKS_EXECUTOR_NAME)
    private val submitted = hazelcast.getMap<String, String>(SUBMITTED_TASKS)

    init {
        dependencies = dependenciesMap
        startupLatch.countDown()
        //We sort the initializers before running to make sure that initialization task are run in the correct order.
        initializers
                .toSortedSet(TaskComparator(initializers))
                .forEach { initializer ->
                    val sw = Stopwatch.createStarted()


                    if (initializer.isRunOnceAcrossCluster()) {
                        val urn = submitted.getOrPut(initializer.name) {
                            executor.schedule(
                                    initializer,
                                    initializer.getInitialDelay(),
                                    initializer.getTimeUnit()
                            ).handler.toUrn()
                        }
                        //By always retrieving the task we can ensure that we wait for initialization tasks kicked
                        //off by different nodes at startup
                        val f: IScheduledFuture<*> = executor.getScheduledFuture<Any>(ScheduledTaskHandler.of(urn))

                        logger.info(
                                "Waiting on initializer {} with initialDelay {} and period {} in time unit (urn = {})",
                                initializer.name,
                                initializer.getInitialDelay(),
                                initializer.getTimeUnit(),
                                urn
                        )

                        f.get()


                    } else {
                        logger.info(
                                "Waiting on initializer {} with initialDelay {} and period {} in time unit (not hazelcast scheduled)",
                                initializer.name,
                                initializer.getInitialDelay(),
                                initializer.getTimeUnit()
                        )

                        initializer.run()
                    }

                    logger.info(
                            "Completed execution of initializer {} in {} millis",
                            initializer.name,
                            sw.elapsed(TimeUnit.MILLISECONDS)
                    )
                }

        logger.info("***********************************************************************")
        logger.info("***                 INITIALIZATION TASK COMPLETED                   ***")
        logger.info("***********************************************************************")
        logger.info("***********************************************************************")
        latch.countDown()
    }

    private val taskFutures = tasks
            .map { task ->
                try {
                    val urn = submitted.getOrPut(task.name) {
                        executor.scheduleAtFixedRate(
                                task,
                                task.getInitialDelay(),
                                task.getPeriod(),
                                task.getTimeUnit()
                        ).handler.toUrn()
                    }
                    logger.info(
                            "Task {} is scheduled with initialDelay {} and period {} in time unit (urn = {})",
                            task.name,
                            task.getInitialDelay(),
                            task.getPeriod(),
                            task.getTimeUnit(),
                            urn
                    )
                } catch (ex: Exception) {
                    logger.error("Unable to schedule task ${task.name}.", ex)
                }
            }

    fun getInitializersCompletedRequirements(): Requirement {
        return initializersCompletedRequirements
    }

    interface HazelcastDependencyAwareTask<T : HazelcastTaskDependencies> {
        fun getDependenciesClass(): Class<out T>

        @JvmDefault
        fun getDependency(): T {
            startupLatch.await()
            return (dependencies[getDependenciesClass()] ?: throw IllegalStateException(
                    "Unable to find dependency ${getDependenciesClass().canonicalName}"
            )) as T
        }
    }

    private class InitializersCompletedRequirements : Requirement {
        override fun getDescription(): String {
            return "All initializers have completed executing!"
        }

        override fun isSatisfied(): Boolean {
            return try {
                latch.await()
                true
            } catch (ex: Exception) {
                logger.error("Unable to satisfy startup requirement.", ex)
                false
            }
        }

    }

    class TaskComparator(
            initializers: Set<HazelcastInitializationTask<*>>
    ) : Comparator<HazelcastInitializationTask<*>> {
        private val initMap = initializers.map { (it.javaClass as Class<*>) to it }.toMap()
        private val ancestorMap = mutableMapOf<Class<*>, MutableSet<Class<*>>>()

        init {
            initializers.forEach { initializer ->
                ancestorMap.getOrPut(initializer.javaClass) {
                    initializer.after().flatMap { clazz: Class<*> ->
                        ancestorMap.getOrPut(clazz) { expandAncestors(clazz).toMutableSet() } + clazz
                    }.toMutableSet()

                }
            }
        }

        override fun compare(a: HazelcastInitializationTask<*>, b: HazelcastInitializationTask<*>): Int {
            val ancestorsOfA = ancestorMap.getValue(a.javaClass)
            val ancestorsOfB = ancestorMap.getValue(b.javaClass)
            return if ((ancestorsOfA.contains(b.javaClass) && ancestorsOfB.contains(a.javaClass))
                    || ancestorsOfA.contains(a.javaClass)
                    || ancestorsOfB.contains(b.javaClass)) {
                logger.error(
                        "Detected cycle in task graph. ${a.javaClass.canonicalName} must happen after ${b.javaClass.canonicalName} and vice-versa."
                )
                throw IllegalStateException(
                        "Detected cycle in task graph. ${a.javaClass.canonicalName} must happen after ${b.javaClass.canonicalName} and vice-versa."
                )
            } else if (ancestorsOfA.contains(b.javaClass)) {
                //1st argument is greater than second argument as it must be initialized after
                1
            } else if (!ancestorsOfB.contains(a.javaClass)) {
                1
            } else {
                //Otherwise initialize as early as reasonable
                -1
            }
        }


        private fun expandAncestors(initial: Class<*>): Set<Class<*>> {
            val ancestors = mutableSetOf<Class<*>>()
            var current = setOf(initial)
            while (current.isNotEmpty()) {
                current = current.flatMap {
                    try {
                        initMap.getValue(it).after()
                    } catch (ex: java.util.NoSuchElementException) {
                        logger.error("Missing dependency: {}", it.canonicalName)
                        throw ex
                    }
                }.toSet()
                ancestors += current
            }
            return ancestors
        }
    }
}
