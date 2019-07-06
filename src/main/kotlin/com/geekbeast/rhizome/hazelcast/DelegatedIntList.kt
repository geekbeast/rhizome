package com.geekbeast.rhizome.hazelcast

/**
 * A quick and dirty delegation wrapper for hazelast serialization of int lists.
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class DelegatedIntList(list: List<Int>) : List<Int> by list {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}