package com.geekbeast.rhizome

/**
 * @author Drew Bailey &lt;drew@openlattice.com&gt;
 */
class KotlinDelegatedStringSet(strings: Set<String>) : Set<String> by strings {
    override fun equals(other: Any?): Boolean {

        return if (other !is Set<*> ) {
            false
        } else {
            this.size == other.size && this.containsAll(other)
        }
    }
}
