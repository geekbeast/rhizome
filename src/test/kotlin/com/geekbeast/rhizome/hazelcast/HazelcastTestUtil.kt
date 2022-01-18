package com.geekbeast.rhizome.hazelcast

import com.hazelcast.config.*
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */

fun startHazelcastInstance(
        name: String,
        group: String,
        streamSerializers: Set<SelfRegisteringStreamSerializer<*>>
): HazelcastInstance {
    val config = Config(name)
    config.clusterName = group
    config.serializationConfig = SerializationConfig()
    streamSerializers.forEach {
        config.serializationConfig.addSerializerConfig(
                SerializerConfig()
                        .setTypeClass(it.clazz)
                        .setImplementation(it)
        )
    }
    return Hazelcast.newHazelcastInstance(config)
}