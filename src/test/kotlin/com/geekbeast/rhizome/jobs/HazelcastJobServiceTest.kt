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

import com.geekbeast.rhizome.tests.bootstrap.RhizomeTests
import com.hazelcast.core.HazelcastInstance
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class HazelcastJobServiceTest : RhizomeTests() {
    companion object {
        private lateinit var hazelcastInstance: HazelcastInstance
        private lateinit var jobService: HazelcastJobService

        @JvmStatic
        @BeforeClass
        fun initHz() {
            hazelcastInstance = rhizome.context.getBean(HazelcastInstance::class.java)
            jobService = HazelcastJobService(hazelcastInstance)
        }
    }

    @Test
    fun testJobService() {
        val ids = setOf(jobService.submitJob(EmptyJob(EmptyJobState(RandomStringUtils.random(5)))),
        jobService.submitJob(EmptyJob(EmptyJobState(RandomStringUtils.random(5)))),
        jobService.submitJob(EmptyJob(EmptyJobState(RandomStringUtils.random(5)))),
        jobService.submitJob(EmptyJob(EmptyJobState(RandomStringUtils.random(5)))))
        Assert.assertTrue( ids.size == 4 )
        val jobs = jobService.getJobs()
        Assert.assertEquals(4, jobs.size)

        jobs.forEach { Assert.assertEquals(5L, jobService.getResultAndDisposeOfTask<Long>(it.key).second) }

        val moreJobs = jobService.getJobs()
        Assert.assertEquals(0, moreJobs.size)
    }

    @Test
    fun testFailedJob() {
        val id = jobService.submitJob(EmptyJob(EmptyJobState(RandomStringUtils.random(5)), fail = true))

        val (job, result) = jobService.getResultAndDisposeOfTask<Long>(id)
        Assert.assertNull( result )
        Assert.assertEquals(JobStatus.CANCELED,job.status)
    }
}