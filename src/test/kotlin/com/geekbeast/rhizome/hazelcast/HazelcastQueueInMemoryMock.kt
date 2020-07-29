package com.geekbeast.rhizome.hazelcast

import com.google.common.collect.Queues
import com.hazelcast.collection.IQueue
import org.mockito.Matchers.any
import org.mockito.Mockito
import java.util.concurrent.TimeUnit


/**
 * Mocks a hazelcast using a concurrent map. It implements put,putIfAbsent, get,set, and [] operator.
 *
 * If additional functionality is required regular mockito mocking behavior applies.
 *
 * @param valueClass A reference to the class of the value used for the map
 * @return A mocked IMap backed by a concurrent map.
 */
fun <V> mockHazelcastQueue(valueClass: Class<V>): IQueue<V> {
    val mock = Mockito.mock<IQueue<*>>(IQueue::class.java) as IQueue<V>
    val backingQueue = Queues.newArrayBlockingQueue<V>(10000)

    Mockito.`when`(mock.put(any(valueClass))).thenAnswer {
        val v = it.arguments[0] as V
        backingQueue.put(v)
    }

    Mockito.`when`(mock.add(any(valueClass))).thenAnswer {
        val v = it.arguments[0] as V
        backingQueue.add(v)
    }

    Mockito.`when`(mock.offer(any(valueClass))).thenAnswer {
        val v = it.arguments[0] as V
        backingQueue.offer(v)
    }

    Mockito.`when`(mock.offer(any(valueClass), any(Long::class.java), any(TimeUnit::class.java))).thenAnswer {
        val v = it.arguments[0] as V
        val timeout = it.arguments[1] as Long
        val timeUnit = it.arguments[2] as TimeUnit
        backingQueue.offer(v, timeout, timeUnit)
    }

    Mockito.`when`(mock.take()).thenAnswer {
        backingQueue.take()
    }

    Mockito.`when`(mock.poll()).thenAnswer {
        backingQueue.poll()
    }

    Mockito.`when`(mock.poll(any(Long::class.java), any(TimeUnit::class.java))).thenAnswer {
        val timeout = it.arguments[0] as Long
        val timeUnit = it.arguments[1] as TimeUnit
        backingQueue.poll(timeout, timeUnit)
    }

    Mockito.`when`(mock.clear()).thenAnswer { backingQueue.clear() }
    Mockito.`when`(mock.size).thenAnswer { backingQueue.size }

    return mock
}
