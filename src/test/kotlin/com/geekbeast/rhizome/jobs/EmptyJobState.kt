package com.geekbeast.rhizome.jobs

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class EmptyJobState(val s :String) : JobState

class EmptyJob( state: EmptyJobState ) : AbstractDistributedJob<Long, EmptyJobState>(state){
    override fun result(): Long? {
        return null
    }

    override fun processNextBatch() {

    }

}