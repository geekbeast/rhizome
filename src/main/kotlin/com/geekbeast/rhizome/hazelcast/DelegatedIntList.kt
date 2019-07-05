package com.geekbeast.rhizome.hazelcast

/**
 * A quick and dirty delegation wrapper for hazelast serialization of int lists.
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class DelegatedIntList(list: List<Int>) : List<Int> by list