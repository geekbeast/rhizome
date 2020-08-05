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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.geekbeast.rhizome.hazelcast.insertIntoUnusedKey
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import com.hazelcast.map.IMap
import com.hazelcast.query.Predicate
import com.hazelcast.query.Predicates
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.Callable

const val JOBS_MAP = "_rhizome_jobs_"
private const val JOB_STATUS = "status"
private const val RESUMABLE = "resumable"
private const val JOBS_EXECUTOR = "_rhizome_job_service_"

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
    protected val durableExecutor = hazelcastInstance.getDurableExecutorService(JOBS_EXECUTOR)
    protected val mapper: ObjectMapper = ObjectMappers.getSmileMapper()


    @Timed
    fun resumeJobs(ids: Set<UUID>) {
        //Due to all the self-serializing operations and the fact this will only ever get called on hazelcast cluster
        //we just load the job and resubmit
        ids.forEach { id ->
            durableExecutor.submitToKeyOwner(jobs[id]!!, id)
        }
    }

    @Timed
    fun <R, S : JobState> submitJob(job: AbstractDistributedJob<R, S>): UUID {
        require(job.status == JobStatus.PENDING) { "Job status must be pending to submit." }

        val id = insertIntoUnusedKey(
                jobs,
                job,
                generate = UUID::randomUUID,
                maxIdle = job.maxBatchDuration,
                maxIdleUnit = job.maxBatchDurationTimeUnit
        )

        //We need to let the job know its own id.
        jobs.executeOnKey(id) {
            val v = it.value
            if (v != null) {
                v.initId(id)
                it.setValue(v)
                if (logger.isDebugEnabled) logger.debug("Set task id $id")
            }
            return@executeOnKey null
        }

        job.initId(id) //This is duplicate of the job in the map being submitted.

        //Submit to durable executor, grab the task id, and update the job with its task id so that task can retrieve.
        val f = durableExecutor.submitToKeyOwner(job, id)
        val taskId = f.taskId

        jobs.executeOnKey(id) {
            val v = it.value
            if (v != null) {
                v.initTaskId(taskId)
                it.setValue(v)
                if (logger.isDebugEnabled) logger.debug("Set task id $taskId for task $id")

            }
            return@executeOnKey null
        }

        //Fail late, but at least give caller feedback. We have to do it in this order since these properties are set once
        job.initTaskId(taskId)
        validateJob(job)

        return id
    }

    fun <T> getResultAndDisposeOfTask(id: UUID): Pair<AbstractDistributedJob<*, *>, T?> {
        val result = durableExecutor.retrieveAndDisposeResult<T?>(getTaskId(id)).get()
        val job = jobs.remove(id)!!

        return job to result
    }

    fun <T> getResult(id: UUID): T? {
        val taskId = getTaskId(id)
        return durableExecutor.retrieveResult<T?>(taskId).get()
    }

    fun getStatus(id: UUID): JobStatus {
        ensureJobExists(id)
        return jobs.executeOnKey(id) { it.value.status }
    }

    @Timed
    fun getJobIds(
            jobStates: Set<JobStatus> = EnumSet.allOf(JobStatus::class.java)
    ): Set<UUID> {
        return jobs
                .keySet(Predicates.and(buildStatesPredicate(jobStates)))
    }

    @Timed
    fun getJobs(
            jobStates: Set<JobStatus> = EnumSet.allOf(JobStatus::class.java),
            resumable: Set<Boolean> = setOf(true, false)
    ): Map<UUID, AbstractDistributedJob<*, *>> {
        return jobs
                .entrySet(Predicates.and(buildStatesPredicate(jobStates), buildResumablePredicate(resumable)))
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

    private fun ensureJobExists(id: UUID) {
        require(jobs.containsKey(id)) {
            "Unable to find job $id"
        }
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

    private fun getTaskId(id: UUID): Long {
        ensureJobExists(id)
        val taskId = jobs.executeOnKey(id) { it.value?.taskId }

        require(taskId != null) {
            "Task id has not yet been assigned for job $id."
        }

        return taskId
    }
}

private const val JOB_NOT_DESERIAZBLE_ERROR = "Submitted job could not be properly deserialized."
private const val JOB_NOT_SERIAZBLE_ERROR = "Submitted job could not be properly serialized."

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
interface JobState

internal fun buildStatesPredicate(
        jobStates: Set<JobStatus>
): Predicate<UUID, AbstractDistributedJob<*, *>> = Predicates.`in`(
        JOB_STATUS,
        *jobStates.toTypedArray()
)

internal fun buildResumablePredicate(
        resumable: Set<Boolean>
): Predicate<UUID, AbstractDistributedJob<*, *>> = Predicates.`in`(RESUMABLE, *resumable.toTypedArray())

internal fun buildIdsPredicate(ids: Collection<UUID>): Predicate<UUID, AbstractDistributedJob<*, *>> = Predicates.`in`(
        JOB_STATUS,
        *ids.toTypedArray()
)

