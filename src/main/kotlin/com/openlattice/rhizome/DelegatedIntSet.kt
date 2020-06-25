package com.openlattice.rhizome

/**
 * @author Drew Bailey &lt;drew@openlattice.com&gt;
 */
class DelegatedIntSet( private val ints: Set<Int> ): Set<Int> by ints {
    override fun equals(other: Any?): Boolean {
        return ints == other
    }

    override fun hashCode(): Int {
        return ints.hashCode()
    }

    override fun toString(): String {
        return ints.toString()
    }
}
