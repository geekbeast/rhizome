package com.geekbeast.rhizome.jobs

import com.openlattice.tasks.HazelcastTaskDependencies

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class ResumeJobDependencies(
        val jobService: HazelcastJobService
) : HazelcastTaskDependencies