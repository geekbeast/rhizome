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

import com.hazelcast.scheduledexecutor.NamedTask
import com.openlattice.tasks.TaskService.HazelcastDependencyAwareTask
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(HazelcastFixedRateTask::class.java)
/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
interface HazelcastFixedRateTask<T : HazelcastTaskDependencies> : Runnable, NamedTask, HazelcastDependencyAwareTask<T> {
    fun getInitialDelay(): Long
    fun getPeriod(): Long
    fun getTimeUnit(): TimeUnit
    fun runTask()
    override fun run() {
        try {
            logger.info("Running scheduled task $name with period ${getPeriod()}")
            runTask()
            logger.info("Completed scheduled task $name.")
        } catch (ex:Exception) {
            logger.error("Error occured while running fixed rate task.", ex)
        }
    }
}