package com.geekbeast.rhizome.hazelcast

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class HazelcastQueueInMemoryMockTest {
    @Test
    fun testQueueMock() {
        val q = mockHazelcastQueue(Int::class.java)
        q.add(5)
        q.put(10)
        q.offer(3, 1000L, TimeUnit.MILLISECONDS)

        Assert.assertEquals (5, q.take() )
        Assert.assertEquals (10, q.take() )
        Assert.assertEquals (3, q.take() )
    }
}