package com.geekbeast.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.SerializationConfig
import com.hazelcast.core.HazelcastInstance
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration
import com.kryptnostic.rhizome.pods.hazelcast.BaseHazelcastInstanceConfigurationPod.clientNetworkConfig

interface IHazelcastClientProvider {
    fun getClient(name: String): HazelcastInstance
}

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class HazelcastClientProvider(
        private val clients: Map<String, HazelcastConfiguration>,
        private val serializationConfig: SerializationConfig
) : IHazelcastClientProvider {
    init {
        clients.values.forEach { client ->
            check(!client.isServer) { "Specified server = true for client config: $client" }
        }
    }

    private val hazelcastClients = clients.mapValues { (name, clientConfig) ->
        val cc = ClientConfig()
                .setNetworkConfig(clientNetworkConfig(clientConfig))
                .setSerializationConfig(serializationConfig)
                .setProperty("hazelcast.logging.type", "slf4j")
        cc.clusterName = clientConfig.group
        HazelcastClient.newHazelcastClient(cc)
    }

    override fun getClient(name: String): HazelcastInstance {
        return hazelcastClients.getValue(name)
    }

}