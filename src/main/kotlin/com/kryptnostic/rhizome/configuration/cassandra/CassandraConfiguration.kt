package com.kryptnostic.rhizome.configuration.cassandra

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.ProtocolOptions
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.kryptnostic.rhizome.configuration.amazon.AmazonConfiguration
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException

data class CassandraConfiguration(
        @JsonProperty(CASSANDRA_COMPRESSION_PROPERTY) val compression: ProtocolOptions.Compression = COMPRESSION_DEFAULT,
        @JsonProperty(CASSANDRA_RANDOM_PORTS_PROPERTY) val randomPorts: Boolean = RANDOM_PORTS_DEFAULT,
        @JsonProperty(CASSANDRA_EMBEDDED_PROPERTY) val embedded: Boolean = EMBEDDED_DEFAULT,
        @JsonProperty(CASSANDRA_SSL_ENABLED) val sslEnabled: Boolean = SSL_ENABLED_DEFAULT,
        @JsonProperty(CASSANDRA_SEED_NODES_PROPERTY) var seedNodes: List<String> = CASSANDRA_SEED_DEFAULT,
        @JsonProperty(CASSANDRA_KEYSPACE_PROPERTY) val keyspace: String = KEYSPACE_DEFAULT,
        @JsonProperty(CASSANDRA_REPLICATION_FACTOR) val replicationFactor: Int = REPLICATION_FACTOR_DEFAULT,
        @JsonProperty(CASSANDRA_CONSISTENCY_LEVEL_PROPERTY) val consistencyLevel: ConsistencyLevel = CONSISTENCY_LEVEL_DEFAULT,
        @JsonProperty(AmazonConfiguration.PROVIDER_PROPERTY) val provider: String? = null,
        @JsonProperty(AmazonConfiguration.AWS_REGION_PROPERTY) val region: String = AmazonConfiguration.AWS_REGION_DEFAULT,
        @JsonProperty(AmazonConfiguration.AWS_NODE_TAG_KEY_PROPERTY) val tagKey: Optional<String> = Optional.absent(),
        @JsonProperty(AmazonConfiguration.AWS_NODE_TAG_VALUE_PROPERTY) val tagValue: Optional<String> = Optional.absent() ) {

    var cassandraSeedNodes = listOf<InetAddress>()

    init {
        if (this.randomPorts) {
            logger.warn("Starting cassandra in test mode")
        }
        if ("aws".equals(this.provider!!, ignoreCase = true)) {
            cassandraSeedNodes = AmazonConfiguration.getNodesWithTagKeyAndValueInRegion(region,
                    tagKey,
                    tagValue,
                    logger)
        } else {
            cassandraSeedNodes = transformToInetAddresses(seedNodes)
        }
    }

    companion object {
        private const val CASSANDRA_COMPRESSION_PROPERTY = "compression"
        private const val CASSANDRA_RANDOM_PORTS_PROPERTY = "random-ports"
        private const val CASSANDRA_EMBEDDED_PROPERTY = "embedded"
        private const val CASSANDRA_SSL_ENABLED = "ssl-enabled"
        private const val CASSANDRA_KEYSPACE_PROPERTY = "keyspace"
        private const val CASSANDRA_REPLICATION_FACTOR = "replication-factor"
        private const val CASSANDRA_SEED_NODES_PROPERTY = "seed-nodes"
        private const val HAZELCAST_WRITE_DELAY_FIELD = "write-delay"
        private const val CASSANDRA_CONSISTENCY_LEVEL_PROPERTY = "consistency-level"

        private val CASSANDRA_SEED_DEFAULT = ImmutableList.of("127.0.0.1")
        private const val KEYSPACE_DEFAULT = "rhizome"
        private const val REPLICATION_FACTOR_DEFAULT = 1
        private val CONSISTENCY_LEVEL_DEFAULT = ConsistencyLevel.QUORUM
        private const val RANDOM_PORTS_DEFAULT = false
        private const val EMBEDDED_DEFAULT = false
        private const val SSL_ENABLED_DEFAULT = false
        private val COMPRESSION_DEFAULT = ProtocolOptions.Compression.NONE

        private val logger = LoggerFactory
                .getLogger(CassandraConfiguration::class.java)

        private fun transformToInetAddresses(addresses: List<String>): List<InetAddress> {
            val builder = ImmutableList.builder<InetAddress>()
            for (str in addresses) {
                try {
                    builder.add(InetAddress.getByName(str))
                } catch (e: UnknownHostException) {
                    logger.error("Could not find host {} specified in cassandra configuration in rhizome.yaml", str, e)
                }

            }
            val list = builder.build()
            return if (list.isEmpty()) {
                ImmutableList.of(InetAddress.getLoopbackAddress())
            } else list
        }
    }

}
