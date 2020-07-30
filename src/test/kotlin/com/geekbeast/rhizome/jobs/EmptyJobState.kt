package com.geekbeast.rhizome.jobs

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.RandomUtils
import java.lang.IllegalStateException
import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class EmptyJobState(val name: String) : JobState

class EmptyJob(
        state: EmptyJobState,
        fail: Boolean = false,
        private val rounds: Int = 10,
        var result : Long? = null
) : AbstractDistributedJob<Long, EmptyJobState>(state) {
    private var round = 0
    var fail: Boolean = fail
        private set

    @JsonCreator
    constructor(
            id: UUID?,
            taskId: Long?,
            status: JobStatus,
            progress: Byte,
            hasWorkRemaining: Boolean,
            state: EmptyJobState,
            result: Long?,
            fail: Boolean
    ) : this(state) {
        initialize(id, taskId, status, progress, hasWorkRemaining)
        this.result = result
        this.fail = fail
    }

    override fun result(): Long? {
        return result
    }

    override fun processNextBatch() {
        Thread.sleep(RandomUtils.nextLong(100, 500))
        result = 5
        if( fail ) {
            abort()
        }
        hasWorkRemaining = (++round < rounds)
    }
}