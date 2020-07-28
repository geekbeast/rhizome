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

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.lang.IllegalStateException
import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class DistributedJobState(
    val progress: Byte,
    val jobStatus: JobStatus,
    val state : JobState
) {
    private lateinit var id : UUID
    private var taskId: Long? = null
    set(value) = if( taskId == null ) taskId = value else throw IllegalStateException("Task is cannot be initialized twice.")

    fun setId( id: UUID ) {
        require(!this::id.isInitialized) {
            "Id can only be initialized once."
        }
    }

    fun getId(): UUID {
        return id
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