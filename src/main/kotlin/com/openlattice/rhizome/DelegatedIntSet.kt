package com.openlattice.rhizome

/**
 * @author Drew Bailey &lt;drew@openlattice.com&gt;
 */
class DelegatedIntSet( ints: Set<Int> ): Set<Int> by ints {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }
}
