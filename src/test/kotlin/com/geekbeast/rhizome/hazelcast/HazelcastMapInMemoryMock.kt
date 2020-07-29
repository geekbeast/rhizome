package com.geekbeast.rhizome.hazelcast

import com.google.common.collect.Maps
import com.hazelcast.map.IMap
import com.hazelcast.internal.serialization.InternalSerializationService
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder
import com.hazelcast.internal.serialization.impl.SerializationServiceV1
import com.hazelcast.query.Predicate
import com.hazelcast.query.impl.CachedQueryEntry
import com.hazelcast.query.impl.getters.Extractors
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer
import org.mockito.Matchers.*
import org.mockito.Mockito
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


private val _ss = DefaultSerializationServiceBuilder().build()
private val registered = mutableSetOf<Class<*>>()
private val registrationLock = ReentrantLock()

/**
 * Mocks a hazelcast using a concurrent map. It implements put,putIfAbsent, get,set, and [] operator.
 *
 * If additional functionality is required regular mockito mocking behavior applies.
 *
 * @param keyClass A reference to the class of the key used for the map
 * @param valueClass A reference to the class of the value used for the map
 * @return A mocked IMap backed by a concurrent map.
 */
fun <K, V> mockHazelcastMap(
        keyClass: Class<K>,
        valueClass: Class<V>,
        streamSerializers: List<SelfRegisteringStreamSerializer<*>> = listOf(),
        ss: InternalSerializationService = _ss,
        extractors: Extractors = Extractors.newBuilder(ss).setAttributeConfigs(listOf()).setClassLoader(keyClass.classLoader).build()
): IMap<K, V> {
    val mock = Mockito.mock<IMap<*, *>>(IMap::class.java) as IMap<K, V>
    val backingMap = Maps.newConcurrentMap<K, V>()
    val ttlMap = Maps.newConcurrentMap<K, Long>()
    val idleMap = Maps.newConcurrentMap<K, IdleInfo>()

//    val ssMap = streamSerializers.associateBy { it.clazz }
//    val keyStreamSerializer = getSerializers(ssMap, keyClass)
//    val valueStreamSerializer = getSerializers(ssMap, valueClass)

    if (ss is SerializationServiceV1) {
        try {
            registrationLock.lock()
            streamSerializers.forEach {
                if (!registered.contains(it.clazz)) {
                    ss.register(it.clazz, it)
                    registered.add(it.clazz)
                }
            }
        } finally {
            registrationLock.unlock()
        }
    }

    Mockito.`when`(mock.put(any(keyClass), any(valueClass))).thenAnswer {
        val k = it.arguments[0] as K
        val v = it.arguments[1] as V
        backingMap.put(k, v)
    }

    Mockito.`when`(mock.putIfAbsent(any(keyClass), any(valueClass))).thenAnswer {
        val k = it.arguments[0] as K
        val v = it.arguments[1] as V
        handleExpiration(k, backingMap, ttlMap)
        handleIdleness(k, backingMap, ttlMap, idleMap)
        backingMap.putIfAbsent(k, v)
    }

    Mockito.`when`(
            mock.putIfAbsent(
                    any(keyClass),
                    any(valueClass),
                    anyLong(),
                    any(TimeUnit::class.java),
                    anyLong(),
                    any(TimeUnit::class.java)
            )
    ).thenAnswer {
        val k = it.arguments[0] as K
        val v = it.arguments[1] as V

        val ttlUnit = it.arguments[3] as TimeUnit
        val ttl = ttlUnit.toNanos(it.arguments[2] as Long)

        val maxIdleTimeUnit = it.arguments[5] as TimeUnit
        val maxIdleTime = maxIdleTimeUnit.toNanos(it.arguments[4] as Long)

        val currentNanos = System.nanoTime()

        //Only start tracking if ttl or maxIdleTime are positive
        if (ttl > 0) ttlMap[k] = currentNanos + ttl
        if (maxIdleTime > 0) idleMap[k] = IdleInfo(maxIdleTime, currentNanos + maxIdleTime)

        backingMap.putIfAbsent(k, v)
    }

    Mockito.`when`(mock.set(any(keyClass), any(valueClass))).thenAnswer {
        val k = it.arguments[0] as K
        val v = it.arguments[1] as V
        handleIdleness(k, backingMap, ttlMap, idleMap)
        backingMap[k] = v
        Unit
    }

    Mockito.`when`(mock.getAll(anySetOf(keyClass))).thenAnswer { invocation ->
        val keys = invocation.arguments[0] as Set<K>
        keys.forEach { k ->
            handleExpiration(k, backingMap, ttlMap)
            handleIdleness(k, backingMap, ttlMap, idleMap)
        }
        keys.associateWith { backingMap[it] }
    }

    Mockito.`when`(mock.get(any(keyClass))).thenAnswer {
        val k = it.arguments[0] as K
        handleExpiration(k, backingMap, ttlMap)
        handleIdleness(k, backingMap, ttlMap, idleMap)
        backingMap[k]
    }

    Mockito.`when`(mock.delete(any())).thenAnswer {
        val k = it.arguments[0] as K
        backingMap.remove(k)
        Unit
    }

    Mockito.`when`(mock.remove(any(keyClass))).thenAnswer {
        val k = it.arguments[0] as K
        handleExpiration(k, backingMap, ttlMap)
        handleIdleness(k, backingMap, ttlMap, idleMap)
        backingMap.remove(k)
    }

    Mockito.`when`(mock.entrySet(any())).thenAnswer { invocation ->
        val p = invocation.arguments[0] as Predicate<K, V>
        handleExpiration(backingMap, ttlMap)
        handleIdleness(backingMap, ttlMap, idleMap)
        backingMap.asSequence()
                .map { CachedQueryEntry<K, V>(ss, ss.toData(it.component1()), it.component2(), extractors) }
                .filter(p::apply)
                .toSet()
    }

    Mockito.`when`(mock.keySet(any())).thenAnswer { it ->
        val p = it.arguments[0] as Predicate<K, V>
        handleExpiration(backingMap, ttlMap)
        handleIdleness(backingMap, ttlMap, idleMap)
        backingMap.asSequence()
                .map { CachedQueryEntry<K, V>(ss, ss.toData(it.component1()), it.component2(), extractors) }
                .filter(p::apply)
                .map { entry -> entry.component1() }
                .toSet()
    }

    Mockito.`when`(mock.values(any())).thenAnswer { it ->
        val p = it.arguments[0] as Predicate<K, V>
        handleExpiration(backingMap, ttlMap)
        handleIdleness(backingMap, ttlMap, idleMap)
        backingMap.asSequence()
                .map { CachedQueryEntry<K, V>(ss, ss.toData(it.component1()), it.component2(), extractors) }
                .filter(p::apply)
                .map { entry -> entry.component1() }
                .toList()
    }

    Mockito.`when`(mock.entries).thenAnswer { backingMap.entries }
    Mockito.`when`(mock.keys).thenAnswer { backingMap.keys }
    Mockito.`when`(mock.values).thenAnswer { backingMap.values }
    Mockito.`when`(mock.size).thenAnswer { backingMap.size }
    Mockito.`when`(mock.count()).thenAnswer { backingMap.count() }
    Mockito.`when`(mock.clear()).thenAnswer { backingMap.clear() }

    return mock
}

/**
 * Gets the correct stream serializer by first trying a lookup and then a linear search if that fails.
 * @param streamSerializers The map of stream serializers.
 * @param clazz The class for which a serializer is desired.
 */
private fun getSerializers(
        streamSerializers: Map<Class<*>, SelfRegisteringStreamSerializer<*>>,
        clazz: Class<*>
) = streamSerializers[clazz]
        ?: streamSerializers.getValue(streamSerializers.keys.first { it.isAssignableFrom(clazz) })

private fun <K, V> handleExpiration(backingMap: MutableMap<K, V>, expirationMap: MutableMap<K, Long>) {
    backingMap.keys -= expirationMap.filterValues { it <= System.nanoTime() }.keys
}

private fun <K, V> handleIdleness(
        k: K,
        backingMap: MutableMap<K, V>,
        ttlMap: MutableMap<K, Long>,
        idleMap: MutableMap<K, IdleInfo>
) {
    val idleInfo = idleMap[k]
    if (idleInfo != null) {
        if (idleInfo.expirationNanos > System.nanoTime()) {
            //Mark data as not idle.
            idleInfo.markNotIdle()
        } else {
            //Expire data from the maps
            backingMap.remove(k)
            idleMap.remove(k)
            ttlMap.remove(k)
        }
    }
}

private fun <K, V> handleIdleness(
        backingMap: MutableMap<K, V>,
        ttlMap: MutableMap<K, Long>,
        idleMap: MutableMap<K, IdleInfo>
) {
    idleMap.forEach { (k, idleInfo) ->
        val idleInfo = idleMap[k]
        if (idleInfo != null) {
            if (idleInfo.expirationNanos > System.nanoTime()) {
                //Mark data as not idle.
                idleInfo.markNotIdle()
            } else {
                //Expire data from the maps
                backingMap.remove(k)
                idleMap.remove(k)
                ttlMap.remove(k)
            }
        }
    }
}

private fun <K, V> handleExpiration(k: K, backingMap: MutableMap<K, V>, ttlMap: MutableMap<K, Long>) {
    val expiration = ttlMap[k]
    if (expiration != null && expiration <= System.nanoTime()) {
        backingMap.remove(k)
        ttlMap.remove(k)
    }
}

/**
 * Not meant to be used externally.
 */
private data class IdleInfo(val maxIdleNanos: Long, var expirationNanos: Long) {
    fun markNotIdle() {
        expirationNanos = System.nanoTime() + maxIdleNanos
    }
}