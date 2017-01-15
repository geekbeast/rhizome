package com.kryptnostic.rhizome.configuration.cassandra;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ProtocolOptions.Compression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.kryptnostic.rhizome.configuration.amazon.AmazonConfiguration;

public class CassandraConfiguration {
    private static final String       CASSANDRA_COMPRESSION_PROPERTY  = "compression";
    private static final String       CASSANDRA_RANDOM_PORTS_PROPERTY = "random-ports";
    private static final String       CASSANDRA_EMBEDDED_PROPERTY     = "embedded";
    private static final String       CASSANDRA_SSL_ENABLED           = "ssl-enabled";
    private static final String       CASSANDRA_KEYSPACE_PROPERTY     = "keyspace";
    private static final String       CASSANDRA_REPLICATION_FACTOR    = "replication-factor";
    private static final String       CASSANDRA_SEED_NODES_PROPERTY   = "seed-nodes";
    private static final String       HAZELCAST_WRITE_DELAY_FIELD     = "write-delay";

    private static final List<String> CASSANDRA_SEED_DEFAULT          = ImmutableList.of( "127.0.0.1" );
    private static final String       KEYSPACE_DEFAULT                = "rhizome";
    private static final int          REPLICATION_FACTOR_DEFAULT      = 1;
    private static final boolean      RANDOM_PORTS_DEFAULT            = false;
    private static final boolean      EMBEDDED_DEFAULT                = true;
    private static final boolean      SSL_ENABLED_DEFAULT             = false;
    private static final String       COMPRESSION_DEFAULT             = "NONE";

    private final boolean             randomPorts;
    private final boolean             embedded;
    private final boolean             sslEnabled;

    private final Compression         compression;
    private List<InetAddress>         cassandraSeedNodes;
    private final String              keyspace;
    private final int                 replicationFactor;
    private int                       writeBackDelay;

    private final String              provider;
    private String                    region;

    private static final Logger       logger                          = LoggerFactory
            .getLogger( CassandraConfiguration.class );

    @JsonCreator
    public CassandraConfiguration(
            @JsonProperty( CASSANDRA_COMPRESSION_PROPERTY ) Optional<String> compression,
            @JsonProperty( CASSANDRA_RANDOM_PORTS_PROPERTY ) Optional<Boolean> randomPorts,
            @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY ) Optional<Boolean> embedded,
            @JsonProperty( CASSANDRA_SSL_ENABLED ) Optional<Boolean> sslEnabled,
            @JsonProperty( CASSANDRA_SEED_NODES_PROPERTY ) Optional<List<String>> cassandraSeedNodes,
            @JsonProperty( CASSANDRA_KEYSPACE_PROPERTY ) Optional<String> keyspace,
            @JsonProperty( CASSANDRA_REPLICATION_FACTOR ) Optional<Integer> replicationFactor,
            @JsonProperty( AmazonConfiguration.PROVIDER_PROPERTY ) Optional<String> provider,
            @JsonProperty( AmazonConfiguration.AWS_REGION_PROPERTY ) Optional<String> region,
            @JsonProperty( AmazonConfiguration.AWS_NODE_TAG_KEY_PROPERTY ) Optional<String> tagKey,
            @JsonProperty( AmazonConfiguration.AWS_NODE_TAG_VALUE_PROPERTY ) Optional<String> tagValue ) {
        this.randomPorts = randomPorts.or( RANDOM_PORTS_DEFAULT );
        if ( this.randomPorts ) {
            logger.warn( "Starting cassandra in test mode" );
        }
        this.embedded = embedded.or( EMBEDDED_DEFAULT );
        this.sslEnabled = sslEnabled.or( SSL_ENABLED_DEFAULT );
        this.keyspace = keyspace.or( KEYSPACE_DEFAULT );
        this.replicationFactor = replicationFactor.or( REPLICATION_FACTOR_DEFAULT );
        // TODO: I don't think this switch statement is required as Jackson will correctly ser/des the enum.
        switch ( compression.or( COMPRESSION_DEFAULT ).toLowerCase() ) {
            case "lz4":
                this.compression = Compression.LZ4;
                break;
            case "snappy":
                this.compression = Compression.SNAPPY;
                break;
            default:
                this.compression = Compression.NONE;
                break;
        }

        this.provider = provider.orNull();
        if ( "aws".equalsIgnoreCase( this.provider ) ) {
            this.region = region.or( AmazonConfiguration.AWS_REGION_DEFAULT );
            this.cassandraSeedNodes = AmazonConfiguration.getNodesWithTagKeyAndValueInRegion( this.region,
                    tagKey,
                    tagValue,
                    logger );
        } else {
            this.cassandraSeedNodes = transformToInetAddresses( cassandraSeedNodes.or( CASSANDRA_SEED_DEFAULT ) );
        }
    }

    private static List<InetAddress> transformToInetAddresses( List<String> addresses ) {
        Builder<InetAddress> builder = ImmutableList.<InetAddress> builder();
        for ( String str : addresses ) {
            try {
                builder.add( InetAddress.getByName( str ) );
            } catch ( UnknownHostException e ) {
                logger.error( "Could not find host {} specified in cassandra configuration in rhizome.yaml", str, e );
            }
        }
        ImmutableList<InetAddress> list = builder.build();
        if ( list.isEmpty() ) {
            return ImmutableList.<InetAddress> of( InetAddress.getLoopbackAddress() );
        }
        return list;
    }

    @JsonProperty( AmazonConfiguration.PROVIDER_PROPERTY )
    public String getProvider() {
        return provider;
    }

    @JsonProperty( AmazonConfiguration.AWS_REGION_PROPERTY )
    public String getAwsRegion() {
        return region;
    }

    @JsonProperty( CASSANDRA_COMPRESSION_PROPERTY )
    public Compression getCompression() {
        return compression;
    }

    @JsonProperty( CASSANDRA_RANDOM_PORTS_PROPERTY )
    public boolean isRandomPorts() {
        return randomPorts;
    }

    @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY )
    public boolean isEmbedded() {
        return embedded;
    }

    @JsonProperty( CASSANDRA_SSL_ENABLED )
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    @JsonProperty( CASSANDRA_SEED_NODES_PROPERTY )
    public List<InetAddress> getCassandraSeedNodes() {
        return cassandraSeedNodes;
    }

    @JsonProperty( CASSANDRA_KEYSPACE_PROPERTY )
    public String getKeyspace() {
        return keyspace;
    }

    @JsonProperty( CASSANDRA_REPLICATION_FACTOR )
    public int getReplicationFactor() {
        return replicationFactor;
    }

    @JsonProperty( HAZELCAST_WRITE_DELAY_FIELD )
    public int getDefaultWriteBackDelay() {
        return writeBackDelay;
    }

}
