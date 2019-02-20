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
import com.hazelcast.scheduledexecutor.DuplicateTaskException
import com.hazelcast.scheduledexecutor.IScheduledFuture
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
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
    private val executor = hazelcast.getScheduledExecutorService(HAZELCAST_SCHEDULED_TASKS_EXECUTOR_NAME)
    private val submitted = hazelcast.getMap<String, String>(SUBMITTED_TASKS)

    init {
//        TaskSchedulerGlobalContext.setApplicationContext(context)
        dependencies = dependenciesMap
        //We sort the initializers before running to make sure that initialization task are run in the correct order.
        initializers
                .toSortedSet()
                .forEach { initializer ->
                    val sw = Stopwatch.createStarted()

                    val urn = submitted[initializer.name] ?: executor.schedule(
                            initializer,
                            initializer.getInitialDelay(),
                            initializer.getTimeUnit()
                    ).handler.toUrn()

                    //By always retrieving the task we can ensure that we wait for initialization tasks kicked
                    //off by different nodes at startup
                    val f: IScheduledFuture<*> = executor.getScheduledFuture<Any>(ScheduledTaskHandler.of(urn))

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

    private val taskFutures = tasks
            .map { task ->
                val urn = submitted[task.name] ?: executor.scheduleAtFixedRate(
                        task,
                        task.getInitialDelay(),
                        task.getPeriod(),
                        task.getTimeUnit()).handler.toUrn()
                logger.info(
                        "Task {} is scheduled with initialDelay {} and period {} in time unit (urn = {})",
                        task.name,
                        task.getInitialDelay(),
                        task.getPeriod(),
                        task.getTimeUnit(),
                        urn
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
}