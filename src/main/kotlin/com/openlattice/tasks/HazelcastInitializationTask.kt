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
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
interface HazelcastInitializationTask<T : HazelcastTaskDependencies> : Runnable, NamedTask, HazelcastDependencyAwareTask<T>, Serializable {
    fun getInitialDelay(): Long

    @JvmDefault
    fun getTimeUnit(): TimeUnit {
        return TimeUnit.MILLISECONDS
    }

    @JvmDefault
    override fun run() {
        initialize(getDependency())
    }

    fun initialize(dependencies: T)

    /**
     * Returns the list of task that this task must happen after
     */
    fun after(): Set<Class<out HazelcastInitializationTask<*>>>

    @JvmDefault
    fun isRunOnceAcrossCluster(): Boolean {
        return false
    }
}