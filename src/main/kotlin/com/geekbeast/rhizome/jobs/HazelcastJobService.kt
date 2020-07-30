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

import com.codahale.metrics.annotation.Timed
import com.dataloom.mappers.ObjectMappers
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.readValue
import com.geekbeast.rhizome.hazelcast.insertIntoUnusedKey
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

const val JOBS_MAP = "_rhizome_jobs_"
const val TASK_IDS_MAP = "_rhizome_task_ids_"
const val JOB_STATUS = "jobStatus"
const val JOB_EXECUTOR = "job_exec"

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Service
class HazelcastJobService(hazelcastInstance: HazelcastInstance) {
    companion object {
        private val logger = LoggerFactory.getLogger(HazelcastJobService::class.java)
    }

    protected val jobs = hazelcastInstance.getMap<UUID, AbstractDistributedJob<*, *>>(JOBS_MAP)
    protected val durableExecutor = hazelcastInstance.getDurableExecutorService(JOB_EXECUTOR)
    protected val mapper = ObjectMappers.getSmileMapper()

    @Timed
    fun <R, S : JobState> submitJob(job: AbstractDistributedJob<R, S>): UUID {
        require(job.status == JobStatus.PENDING) { "Job status must be pending to submit." }
        validateJob(job)
        
        val id = insertIntoUnusedKey(jobs, job, UUID::randomUUID)
        val f = durableExecutor.submitToKeyOwner(job, id)

        val taskId = f.taskId
        jobs.executeOnKey(id) {
            val v = it.value
            if (v != null) {
                v.initTaskId(taskId)
                it.setValue(v)
            }
        }

        return id
    }

    @Timed
    fun getJobs(
            jobStates: Set<JobStatus> = EnumSet.allOf(JobStatus::class.java)
    ): Map<UUID, AbstractDistributedJob<*, *>> {
        return jobs
                .entrySet(Predicates.and(buildStatesPredicate(jobStates)))
                .map { it.toPair() }
                .toMap()
    }

    @Timed
    fun getJobs(
            ids: Collection<UUID>,
            jobStates: Set<JobStatus> = EnumSet.allOf(JobStatus::class.java)
    ): Map<UUID, AbstractDistributedJob<*, *>> {
        return jobs
                .entrySet(Predicates.and(buildIdsPredicate(ids), buildStatesPredicate(jobStates)))
                .map { it.toPair() }
                .toMap()
    }

    private fun <R, S : JobState> validateJob(job: AbstractDistributedJob<R, S>) {
        val bytes = try {
            mapper.writeValueAsBytes(job)
        } catch (ex: Exception) {
            logger.error(JOB_NOT_DESERIAZBLE_ERROR, ex)
            throw IllegalArgumentException(JOB_NOT_DESERIAZBLE_ERROR)
        }

        try {
            mapper.readValue<AbstractDistributedJob<R, S>>(bytes)
        } catch (ex: Exception) {
            logger.error(JOB_NOT_DESERIAZBLE_ERROR, ex)
            throw IllegalArgumentException(JOB_NOT_SERIAZBLE_ERROR)
        }
    }
}

private const val JOB_NOT_DESERIAZBLE_ERROR = "Submitted job could not be properly deserialized."
private const val JOB_NOT_SERIAZBLE_ERROR = "Submitted job could not be properly serialized."

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
interface JobState

enum class JobStatus {
    PENDING,
    FINISHED,
    RUNNING,
    STOPPING,
    CANCELED
}

internal fun buildStatesPredicate(
        jobStates: Set<JobStatus>
): Predicate<UUID, AbstractDistributedJob<*, *>> = Predicates.`in`(
        JOB_STATUS,
        *jobStates.toTypedArray()
)

internal fun buildIdsPredicate(ids: Collection<UUID>): Predicate<UUID, AbstractDistributedJob<*, *>> = Predicates.`in`(
        JOB_STATUS,
        *ids.toTypedArray()
)

