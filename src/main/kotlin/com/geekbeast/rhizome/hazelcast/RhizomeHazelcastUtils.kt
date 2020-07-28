package com.geekbeast.rhizome.hazelcast

import com.hazelcast.core.IMap
import java.util.function.Supplier
import javax.annotation.processing.Generated

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
fun <K, V> insertIntoUnusedKey(m: IMap<K, V>, value: V, generate : () -> K ): K {
    var key = generate()
    while (m.putIfAbsent(key, value) != null) {
        key = generate()
    }
    return key
}