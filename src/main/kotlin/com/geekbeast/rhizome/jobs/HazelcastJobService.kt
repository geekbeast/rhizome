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
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.geekbeast.rhizome.hazelcast.insertIntoUnusedKey
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
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
    protected val jobs = hazelcastInstance.getMap<UUID, AbstractDistributedJob<*, *>>(JOBS_MAP)
    protected val durableExecutor = hazelcastInstance.getDurableExecutorService(JOB_EXECUTOR)

    @Timed
    fun <T, S : JobState> submitJob(job: AbstractDistributedJob<T, S>): UUID {
        require(job.status == JobStatus.PENDING) { "Job status must be pending to submit." }
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
}

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

