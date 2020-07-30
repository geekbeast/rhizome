package com.geekbeast.rhizome.hazelcast


import com.hazelcast.map.IMap
import java.util.concurrent.TimeUnit

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
fun <K, V> insertIntoUnusedKey(
        m: IMap<K, V>, value: V,
        ttl: Long = -1,
        ttlUnit: TimeUnit = TimeUnit.MILLISECONDS,
        maxIdle: Long = -1,
        maxIdleUnit: TimeUnit = TimeUnit.MILLISECONDS,
        generate: () -> K
): K {
    var key = generate()
    while (m.putIfAbsent(key, value, ttl, ttlUnit, maxIdle, maxIdleUnit) != null) {
        key = generate()
    }
    return key
}