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

package com.openlattice.tasks.pods

import com.hazelcast.core.HazelcastInstance
import com.openlattice.tasks.HazelcastFixedRateTask
import com.openlattice.tasks.HazelcastInitializationTask
import com.openlattice.tasks.HazelcastTaskDependencies
import com.openlattice.tasks.TaskService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.inject.Inject


private val logger = LoggerFactory.getLogger(TaskSchedulerBootstrapPod::class.java)

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Configuration
@Import(TaskSchedulerBootstrapPod::class)
class TaskSchedulerPod {
    @Inject
    private lateinit var context: ApplicationContext

    @Inject
    private lateinit var hazelcastInstance: HazelcastInstance

    @Inject
    private lateinit var tasks: MutableSet<HazelcastFixedRateTask<*>>

    @Inject
    private lateinit var initializers: MutableSet<HazelcastInitializationTask<*>>

    @Inject
    private lateinit var dependencies: MutableSet<HazelcastTaskDependencies>

    @Bean
    fun taskSchedulerService(): TaskService {
        val dependenciesMap : Map<Class<*>, HazelcastTaskDependencies> = dependencies
                .filter { it !is TaskSchedulerBootstrapPod.NoOpDependencies }
                .groupBy { it.javaClass as Class<*> }
                .mapValues {
                    if (it.value.size > 1) {
                        logger.error(
                                "Encountered {} dependencies of type {}. Please resolve ambiguity.",
                                it.value.size,
                                it.key.canonicalName
                        )
                        throw IllegalStateException(
                                "Encountered ${it.value.size} dependencies of type ${it.key.canonicalName}. Please resolve ambiguity."
                        )
                    } else {
                        it.value.first()
                    }
                }

        val validTasks = tasks.filter { it.name != NO_OP_TASK_NAME }

        validTasks.forEach { task ->
            if (!dependenciesMap.contains(task.getDependenciesClass())) {
                logger.error("Dependencies missing for task {}", task.name)
                throw IllegalStateException("Dependencies missing for task ${task.name}")
            }
        }

        val validInitializers = initializers.filter { it.name != NO_OP_INITIALIZER_NAME }
        validInitializers.forEach { initializer ->
            if (!dependenciesMap.contains(initializer.getDependenciesClass())) {
                logger.error("Dependencies missing for initializer {}", initializer.name)
                throw IllegalStateException("Dependencies missing for initializer ${initializer.name}")
            }
        }

        return TaskService(
                context,
                dependenciesMap,
                validTasks.toSet(),
                validInitializers.toSet(),
                hazelcastInstance
        )
    }
}