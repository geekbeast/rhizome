package com.geekbeast.rhizome.jobs

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class EmptyJobState(val s: String) : JobState

class EmptyJob(state: EmptyJobState) : AbstractDistributedJob<Long, EmptyJobState>(state) {

    @JsonCreator
    constructor(
            id: UUID,
            taskId: Long,
            status: JobStatus,
            progress: Byte,
            hasWorkRemaining: Boolean,
            state: EmptyJobState
    ) : this(state) {
        initialize(id, taskId, status, progress, hasWorkRemaining)
    }

    override fun result(): Long? {
        return null
    }

    override fun processNextBatch() {

    }
}