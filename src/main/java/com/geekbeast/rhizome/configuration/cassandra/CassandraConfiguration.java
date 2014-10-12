package com.geekbeast.rhizome.configuration.cassandra;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class CassandraConfiguration {
    private static final String CASSANDRA_EMBEDDED_PROPERTY = "embedded";
    private static final String CASSANDRA_KEYSPACE_PROPERTY = "keyspace";
    private static final String CASSANDRA_REPLICATION_FACTOR = "replication-factor";
    private static final String CASSANDRA_SEED_NODES_PROPERTY = "seed-nodes";
    
    private static final List<String> CASSANDRA_SEED_DEFAULT = ImmutableList.of("127.0.0.1");
    private static final String KEYSPACE_DEFAULT = "rhizome";
    private static final int REPLICATION_FACTOR_DEFAULT = 2;
    private static final boolean EMBEDDED_DEFAULT = true;
    
    private final boolean embedded; 

    private final List<String> cassandraSeedNodes;
    private final String keyspace;
    private final int replicationFactor;
    
    @JsonCreator
    public CassandraConfiguration(  
            @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY ) Optional<Boolean> embedded ,
            @JsonProperty( CASSANDRA_SEED_NODES_PROPERTY ) Optional<List<String>> cassandraSeedNodes ,
            @JsonProperty( CASSANDRA_KEYSPACE_PROPERTY ) Optional<String> keyspace ,
            @JsonProperty( CASSANDRA_REPLICATION_FACTOR ) Optional<Integer> replicationFactor ) {
        
        this.embedded = embedded.or( EMBEDDED_DEFAULT );
        this.cassandraSeedNodes = cassandraSeedNodes.or( CASSANDRA_SEED_DEFAULT );
        this.keyspace = keyspace.or( KEYSPACE_DEFAULT );
        this.replicationFactor = replicationFactor.or( REPLICATION_FACTOR_DEFAULT );
    }
    
    @JsonProperty( CASSANDRA_EMBEDDED_PROPERTY ) 
    public boolean isEmbedded() {
        return embedded;
    }
    
    @JsonProperty( CASSANDRA_SEED_NODES_PROPERTY ) 
    public List<String> getCassandraSeedNodes() {
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

}
