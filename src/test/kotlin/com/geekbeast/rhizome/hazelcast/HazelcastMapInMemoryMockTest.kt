package com.geekbeast.rhizome.hazelcast

import org.apache.commons.lang3.RandomUtils
import org.junit.Assert
import org.junit.Test

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class HazelcastMapInMemoryMockTest {

    @Test
    fun testHazelcastMapInMemoryMock() {
        val map = mockHazelcastMap(String::class.java, Long::class.java)

        val k = "abc1234"
        val k2 = "abc1235"
        val k3 = "abc1236"
        val v = RandomUtils.nextLong()
        val v2 = RandomUtils.nextLong()
        val v3 = RandomUtils.nextLong()

        Assert.assertEquals(null, map.put(k, v))
        Assert.assertEquals(v, map.get(k))
        Assert.assertEquals(v, map[k])

        Assert.assertEquals(v, map.putIfAbsent(k, v2))
        Assert.assertEquals(null, map.putIfAbsent(k2, v2))
        Assert.assertEquals(v2, map.get(k2))
        Assert.assertEquals(v2, map[k2])

        map.set(k3, v3)
        Assert.assertEquals(v3, map.get(k3))
        Assert.assertEquals(v3, map[k3])

    }
}