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

import com.geekbeast.serializer.serializer.AbstractJacksonSerializationTest
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
internal class JobSerializationTests : AbstractJacksonSerializationTest<EmptyJob>() {
    override fun getSampleData(): EmptyJob {
        val job = EmptyJob(EmptyJobState(RandomStringUtils.random(5)))
        job.initTaskId(RandomUtils.nextLong())
        job.initId(UUID.randomUUID())
        return job
    }

    override fun logResult(result: SerializationResult<EmptyJob>?) {
        logger.info("Json: ${result?.jsonString}")
    }

    override fun getClazz(): Class<EmptyJob> = EmptyJob::class.java
}

