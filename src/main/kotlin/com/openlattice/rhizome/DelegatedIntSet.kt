package com.openlattice.rhizome

/**
 * @author Drew Bailey &lt;drew@openlattice.com&gt;
 */
class DelegatedIntSet( ints: Set<Int> ): Set<Int> by ints {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (!(other is DelegatedIntSet)) return false
        if (other.size != size) return false
        return containsAll(other)
    }
}
