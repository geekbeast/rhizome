package com.geekbeast.rhizome.hazelcast

import com.google.common.collect.Maps
import com.hazelcast.core.IMap
import com.hazelcast.query.Predicate
import org.mockito.Matchers.any
import org.mockito.Mockito


/**
 * Mocks a hazelcast using a concurrent map. It implements put,putIfAbsent, get,set, and [] operator.
 *
 * If additional functionality is required regular mockito mocking behavior applies.
 *
 * @param keyClass A reference to the class of the key used for the map
 * @param valueClass A reference to the class of the value used for the map
 * @return A mocked IMap backed by a concurrent map.
 */
fun <K,V> mockHazelcastMap(keyClass:Class<K>, valueClass: Class<V>) : IMap<K,V> {
    val mock =  Mockito.mock<IMap<*,*>>( IMap::class.java) as IMap<K,V>
    val backingMap = Maps.newConcurrentMap<K,V>()

    Mockito.`when`(mock.put(any(keyClass), any(valueClass))).thenAnswer {
        val k = it.arguments[0] as K
        val v  = it.arguments[1] as V
        backingMap.put( k, v )
    }

    Mockito.`when`(mock.putIfAbsent(any(keyClass), any(valueClass))).thenAnswer {
        val k = it.arguments[0] as K
        val v  = it.arguments[1] as V
        backingMap.putIfAbsent( k, v )
    }


    Mockito.`when`(mock.set(any(keyClass), any(valueClass))).thenAnswer {
        val k = it.arguments[0] as K
        val v  = it.arguments[1] as V
        backingMap[k] = v
        Unit
    }

    Mockito.`when`(mock.get(any(keyClass))).thenAnswer {
        val k = it.arguments[0] as K
        backingMap[k]
    }

    Mockito.`when`(mock.delete(any(keyClass))).thenAnswer {
        val k = it.arguments[0] as K
        backingMap.remove(k)
        Unit
    }

    Mockito.`when`(mock.remove(any(keyClass))).thenAnswer {
        val k = it.arguments[0] as K
        backingMap.remove(k)
    }

    Mockito.`when`(mock.entrySet(any(Predicate::class.java))).thenAnswer {
        val p = it.arguments[0] as Predicate<K,V>
        backingMap.asSequence().filter( p::apply ).toSet()
    }

    Mockito.`when`(mock.keySet(any(Predicate::class.java))).thenAnswer { it ->
        val p = it.arguments[0] as Predicate<K,V>
        backingMap.asSequence().filter( p::apply ).map{ entry -> entry.component1() }.toSet()
    }

    Mockito.`when`(mock.values(any(Predicate::class.java))).thenAnswer { it ->
        val p = it.arguments[0] as Predicate<K,V>
        backingMap.asSequence().filter( p::apply ).map{ entry -> entry.component1() }.toList()
    }
    
    return mock
}
