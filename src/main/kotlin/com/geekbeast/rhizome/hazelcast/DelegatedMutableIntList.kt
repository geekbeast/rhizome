package com.geekbeast.rhizome.hazelcast

/**
 * A quick and dirty delegation wrapper for hazelast serialization of int lists.
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class DelegatedMutableIntList(list: MutableList<Int>) : MutableList<Int> by list