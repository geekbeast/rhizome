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

public class CassandraConfiguration {
    private static final String       CASSANDRA_COMPRESSION_PROPERTY = "compression";
    private static final String       CASSANDRA_EMBEDDED_PROPERTY   = "embedded";
    private static final String       CASSANDRA_KEYSPACE_PROPERTY   = "keyspace";
    private static final String       CASSANDRA_REPLICATION_FACTOR  = "replication-factor";
    private static final String       CASSANDRA_SEED_NODES_PROPERTY = "seed-nodes";
    private static final String       HAZELCAST_WRITE_DELAY_FIELD    = "write-delay";

    private static final List<String> CASSANDRA_SEED_DEFAULT        = ImmutableList.of( "127.0.0.1" );
    private static final String       KEYSPACE_DEFAULT              = "rhizome";
    private static final int          REPLICATION_FACTOR_DEFAULT    = 2;
    private static final boolean      EMBEDDED_DEFAULT              = true;
    private static final String       COMPRESSION_DEFAULT            = "NONE";

    private final boolean             embedded;

    private final Compression         compression;
    private final List<InetAddress>   cassandraSeedNodes;
    private final String              keyspace;
    private final int                 replicationFactor;
    private int                       writeBackDelay;

    private static final Logger       logger                        = LoggerFactory
                                                                            .getLogger( CassandraConfiguration.class );

    @JsonCreator
    public CassandraConfiguration(
            @JsonProperty( CASSANDRA_COMPRESSION_PROPERTY ) Optional<String> compression,
            @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY ) Optional<Boolean> embedded,
            @JsonProperty( CASSANDRA_SEED_NODES_PROPERTY ) Optional<List<String>> cassandraSeedNodes,
            @JsonProperty( CASSANDRA_KEYSPACE_PROPERTY ) Optional<String> keyspace,
            @JsonProperty( CASSANDRA_REPLICATION_FACTOR ) Optional<Integer> replicationFactor) {
        this.embedded = embedded.or( EMBEDDED_DEFAULT );
        this.cassandraSeedNodes = transformToInetAddresses( cassandraSeedNodes.or( CASSANDRA_SEED_DEFAULT ) );
        this.keyspace = keyspace.or( KEYSPACE_DEFAULT );
        this.replicationFactor = replicationFactor.or( REPLICATION_FACTOR_DEFAULT );
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

    @JsonProperty( CASSANDRA_COMPRESSION_PROPERTY )
    public Compression getCompression() {
        return compression;
    }

    @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY )
    public boolean isEmbedded() {
        return embedded;
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
