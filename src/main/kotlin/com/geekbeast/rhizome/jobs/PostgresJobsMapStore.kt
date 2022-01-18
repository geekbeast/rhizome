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
import com.geekbeast.mappers.mappers.ObjectMappers
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.geekbeast.postgres.PostgresColumnDefinition
import com.geekbeast.postgres.PostgresDatatype
import com.geekbeast.postgres.PostgresTableDefinition
import com.geekbeast.postgres.mapstores.AbstractBasePostgresMapstore
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.lang3.RandomUtils
import org.springframework.stereotype.Component
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

/**
 * Provides a postgres based backing map store for jobs.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Component
class PostgresJobsMapStore @JvmOverloads constructor(
        hds: HikariDataSource,
        private val mapper: ObjectMapper = ObjectMappers.getJsonMapper()
) : AbstractBasePostgresMapstore<UUID, DistributableJob<*>>(JOBS_MAP, JOBS, hds, BATCH_SIZE) {
    companion object{
        @JvmField
        val JOBS: PostgresTableDefinition = PostgresTableDefinition("jobs").addColumns(ID_COLUMN, JOB_COLUMN)
    }
    override fun generateTestKey(): UUID = UUID.randomUUID()
    override fun generateTestValue(): DistributableJob<*> {
        val job = EmptyJob(EmptyJobState("testValue"))
        job.initId(UUID.randomUUID())
        job.initTaskId(RandomUtils.nextLong(0, Long.MAX_VALUE))
        return job
    }

    override fun mapToKey(rs: ResultSet): UUID = rs.getObject(ID_FIELD, UUID::class.java)

    @Timed
    override fun mapToValue(rs: ResultSet): DistributableJob<*> = mapper.readValue(rs.getString(JOB_FIELD))

    override fun bind(ps: PreparedStatement, key: UUID, value: DistributableJob<*>?) {
        val json = mapper.writeValueAsString(value)
        ps.setObject(1, key)
        ps.setString(2, json)
        ps.setString(3, json)
    }

    override fun bind(ps: PreparedStatement, key: UUID, offset: Int): Int {
        ps.setObject(offset, key)
        return offset+1
    }
}

const val ID_FIELD = "id"
const val JOB_FIELD = "job"
private val ID_COLUMN = PostgresColumnDefinition(ID_FIELD, PostgresDatatype.UUID).primaryKey().notNull()
private val JOB_COLUMN = PostgresColumnDefinition(JOB_FIELD, PostgresDatatype.JSONB).notNull()
