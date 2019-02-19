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

import com.hazelcast.core.HazelcastInstance
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

const val HAZELCAST_SCHEDULED_TASKS_EXECUTOR_NAME = "hazelcast_scheduled_tasks"

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class TaskSchedulerService(
        context: ApplicationContext,
        tasks: Set<HazelcastFixedRateTask<*>>,
        private val initializers: Set<HazelcastInitializationTask<*>>,
        hazelcast: HazelcastInstance
) {
    private val executor = hazelcast.getScheduledExecutorService(HAZELCAST_SCHEDULED_TASKS_EXECUTOR_NAME)

    companion object {
        private lateinit var context: ApplicationContext
        private val logger = LoggerFactory.getLogger(TaskSchedulerService::class.java)

        @JvmStatic
        private fun <T : HazelcastTaskDependencies> getTaskDependencies(dependency: Class<T>): T {
            try {
                return context.getBean(dependency)
            } catch (ex: Exception) {
                logger.error("Encountered error while trying to retrieve h")
                throw ex
            }
        }

        private fun setContext(appContext: ApplicationContext) {
            if (!::context.isInitialized) {
                context = appContext
            }
        }
    }

    init {
        setContext(context)
        tasks.forEach { task ->
            executor.scheduleAtFixedRate(task, task.getInitialDelay(), task.getPeriod(), task.getTimeUnit())
        }

        initializers.forEach { initializer ->
            executor.schedule(initializer, initializer.getInitialDelay(), initializer.getTimeUnit())
        }
    }

    interface HazelcastDependencyAwareTask<T : HazelcastTaskDependencies> {
        fun getDependenciesClass(): Class<out T>

        @JvmDefault
        fun getDependency(): T {
            return TaskSchedulerService.getTaskDependencies(getDependenciesClass())
        }
    }
}