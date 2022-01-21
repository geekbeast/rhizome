package com.geekbeast.rhizome.jobs

import com.geekbeast.tasks.HazelcastInitializationTask
import java.util.*

const val RESUME_JOBS_TASK = "_rhizome_resume_jobs_task_"

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class ResumeJobsInitializationTask : HazelcastInitializationTask<ResumeJobDependencies> {
    override fun getInitialDelay(): Long = 0

    override fun initialize(dependencies: ResumeJobDependencies) {
        val jobs = dependencies.jobService.getJobs(EnumSet.of(JobStatus.RUNNING, JobStatus.PENDING), setOf(true))
        dependencies.jobService.resumeJobs(jobs.keys)
    }

    override fun after(): Set<Class<out HazelcastInitializationTask<*>>> = setOf()
    override fun getName(): String = RESUME_JOBS_TASK

    override fun getDependenciesClass(): Class<out ResumeJobDependencies> = ResumeJobDependencies::class.java
}