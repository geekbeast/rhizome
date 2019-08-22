package com.geekbeast.rhizome.hazelcast

class DelegatedIntListTest {

    fun testEquals() {
        val dlistOne = DelegatedIntList(listOf(1,2,3,4))
        val dlistTwo = DelegatedIntList(listOf(1,2,3,4))

        assert(dlistOne.equals(dlistTwo))
    }

}
