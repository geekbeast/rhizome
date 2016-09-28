package com.kryptnostic.rhizome.configuration.cassandra;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class CassandraConfiguration {
    private static final String       CASSANDRA_COMPRESSION_PROPERTY = "compression";
    private static final String       CASSANDRA_EMBEDDED_PROPERTY    = "embedded";
    private static final String       CASSANDRA_KEYSPACE_PROPERTY    = "keyspace";
    private static final String       CASSANDRA_REPLICATION_FACTOR   = "replication-factor";
    private static final String       CASSANDRA_SEED_NODES_PROPERTY  = "seed-nodes";
    private static final String       HAZELCAST_WRITE_DELAY_FIELD    = "write-delay";
    private static final String       CASSANDRA_PORT                 = "port";

    private static final String       CASSANDRA_PROVIDER_PROPERTY    = "provider";
    private static final String       AWS_REGION_PROPERTY            = "region";
    private static final String       AWS_NODE_TAG_KEY_PROPERTY      = "node-tag-key";
    private static final String       AWS_NODE_TAG_VALUE_PROPERTY    = "node-tag-value";

    private static final List<String> CASSANDRA_SEED_DEFAULT         = ImmutableList.of( "127.0.0.1" );
    private static final String       KEYSPACE_DEFAULT               = "rhizome";
    private static final int          REPLICATION_FACTOR_DEFAULT     = 2;
    private static final boolean      EMBEDDED_DEFAULT               = true;
    private static final String       COMPRESSION_DEFAULT            = "NONE";
    private static final String       REGION_DEFAULT                 = "us-west-1";

    private final boolean             embedded;

    private final Compression         compression;
    private List<InetAddress>         cassandraSeedNodes;
    private final String              keyspace;
    private final int                 replicationFactor;
    private int                       writeBackDelay;

    private final String              provider;
    private final String              nodeTagKey;
    private final String              nodeTagValue;
    private String                    region;

    private static final Logger       logger                         = LoggerFactory
                                                                             .getLogger( CassandraConfiguration.class );

    @JsonCreator
    public CassandraConfiguration(
            @JsonProperty( CASSANDRA_COMPRESSION_PROPERTY ) Optional<String> compression,
            @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY ) Optional<Boolean> embedded,
            @JsonProperty( CASSANDRA_SEED_NODES_PROPERTY ) Optional<List<String>> cassandraSeedNodes,
            @JsonProperty( CASSANDRA_KEYSPACE_PROPERTY ) Optional<String> keyspace,
            @JsonProperty( CASSANDRA_REPLICATION_FACTOR ) Optional<Integer> replicationFactor,
            @JsonProperty( CASSANDRA_PROVIDER_PROPERTY ) Optional<String> provider,
            @JsonProperty( AWS_REGION_PROPERTY ) Optional<String> region,
            @JsonProperty( AWS_NODE_TAG_KEY_PROPERTY ) Optional<String> tagKey,
            @JsonProperty( AWS_NODE_TAG_VALUE_PROPERTY ) Optional<String> tagValue) {
        this.embedded = embedded.or( EMBEDDED_DEFAULT );
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
        this.nodeTagKey = tagKey.orNull();
        this.nodeTagValue = tagValue.orNull();
        if ( "aws".equals( provider ) ) {
            this.cassandraSeedNodes = getSeeds();
            this.region = region.or( REGION_DEFAULT );
        } else {
            this.cassandraSeedNodes = transformToInetAddresses( cassandraSeedNodes.or( CASSANDRA_SEED_DEFAULT ) );
        }
    }

    public List<InetAddress> getSeeds() {
        AmazonEC2Async ec2 = AmazonEC2AsyncClientBuilder.standard()
                .withRegion( this.region )
                .build();
        Filter tagValue = new Filter()
                .withName( "tag-value" )
                .withValues( nodeTagValue );
        Filter tagKey = new Filter()
                .withName( "tag-key" )
                .withValues( nodeTagKey );
        DescribeInstancesRequest req = new DescribeInstancesRequest().withFilters( tagKey, tagValue );

        DescribeInstancesResult describeInstances = ec2.describeInstances( req );

        List<Reservation> reservations = describeInstances.getReservations();
        ArrayList<InetAddress> addresses = new ArrayList<>();
        for ( Reservation res : reservations ) {
            for ( Instance instance : res.getInstances() ) {
                try {
                    if ( instance.getState().getCode() < 17 ) {
                        addresses.add( InetAddress.getByName( instance.getPrivateIpAddress() ) );
                    }
                } catch ( UnknownHostException e ) {
                    logger.error( "Couldn't identify host {}", instance.getPrivateIpAddress(), e );
                }
            }
        }
        return addresses;
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

    @JsonProperty( CASSANDRA_PROVIDER_PROPERTY )
    public String getProvider() {
        return provider;
    }

    @JsonProperty( AWS_REGION_PROPERTY )
    public String getAwsRegion() {
        return region;
    }

    @JsonProperty( AWS_NODE_TAG_KEY_PROPERTY )
    public String getNodeTagKey() {
        return nodeTagKey;
    }

    @JsonProperty( AWS_NODE_TAG_VALUE_PROPERTY )
    public String getNodeTagValue() {
        return nodeTagValue;
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
