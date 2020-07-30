/*
 * Copyright (C) 2020. OpenLattice, Inc.
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

package com.geekbeast.rhizome.jobs

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import com.hazelcast.map.IMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.lang.Thread.interrupted
import java.util.*
import java.util.concurrent.Callable

/**
 * This class allows the execution of long running background jobs on the Hazelcast cluster.
 *
 * @param state The state of the job that can be retrieved by any hazelcast client.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
abstract class AbstractDistributedJob<R, S : JobState>(
        state: S
) : Callable<R?>, HazelcastInstanceAware {
    var state: S = state
        protected set

    private lateinit var _id: UUID
    val id: UUID
        get() = _id

    private var _taskId: Long? = null
    val taskId: Long
        get() = _taskId!!

    var progress: Byte = 0
        protected set

    var status: JobStatus = JobStatus.PENDING
        protected set

    var hasWorkRemaining: Boolean = true
        protected set

    @Transient
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transient
    private lateinit var hazelcastInstance: HazelcastInstance

    @Transient
    private lateinit var jobs: IMap<UUID, AbstractDistributedJob<*, *>>

    /**
     * Internal API for doing a one time assignment of the hazelcast generated task id.
     * @param taskId The id that was assigned to this task.
     */
    internal fun initTaskId(taskId: Long) {
        this._taskId = if (_taskId == null) taskId else throw IllegalStateException("Task id can only be assigned once.")
    }

    /**
     * Internal API for doing a one time assignment of the randomly generated uuid for the task.
     * @param id The key of the job in the jobs maps.
     */
    internal fun initId(id:UUID ) {
        this._id = if (this::_id.isInitialized) throw IllegalStateException("Task id can only be assigned once.") else id
    }

    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance
        this.jobs = hazelcastInstance.getMap(JOBS_MAP)
    }

    abstract fun result(): R?
    abstract fun next()


    open fun abort(ex: Exception) {
        throw ex
    }

    final override fun call(): R? {
        while (!interrupted() && hasWorkRemaining) {
            try {
                next()
            } catch (ex: InterruptedException) {
                logger.warn("Distributed job $taskId was interrupted.")
                abort(ex)
            } finally {
                publishJobState()
            }
        }

        return if (hasWorkRemaining) {
            logger.warn("Distributed job $taskId was interrupted or canceled.")
            null
        } else {
            result()
        }
    }

    private fun initializeTaskId() {
        require(_taskId == null) { "Task id can only be initialized once from map." }
        initTaskId(jobs.executeOnKey(id) { it.value.taskId })
    }

    protected fun publishJobState() {
        val job = this
        jobs.set(id, job)
    }
}
