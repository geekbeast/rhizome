package com.geekbeast.rhizome.configuration.hyperdex;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class HyperdexConfiguration {
    private static final String HYPERDEX_SPACE_PROPERTY = "space";
    private static final String HYPERDEX_TOLERATE_FAILURES_PROPERTY = "tolerate";
    private static final String HYPERDEX_COORDINATORS_PROPERTY = "seed-nodes";
    
    private static final List<String> HYPERDEX_SEED_DEFAULT = ImmutableList.of("127.0.0.1");
    private static final String SPACE_DEFAULT = "HYPERDEX";
    private static final int REPLICATION_TOLERATE_FAILURES_DEFAULT = 2; 
    

    private final List<String> coordinators;
    private final String keyspace;
    private final int replicationFactor;
    
    @JsonCreator
    public HyperdexConfiguration(  
            @JsonProperty( HYPERDEX_COORDINATORS_PROPERTY ) Optional<List<String>> coordinators ,
            @JsonProperty( HYPERDEX_SPACE_PROPERTY ) Optional<String> keyspace ,
            @JsonProperty( HYPERDEX_TOLERATE_FAILURES_PROPERTY ) Optional<Integer> replicationFactor ) {
        
        this.coordinators = coordinators.or( HYPERDEX_SEED_DEFAULT );
        this.keyspace = keyspace.or( SPACE_DEFAULT );
        this.replicationFactor = replicationFactor.or( REPLICATION_TOLERATE_FAILURES_DEFAULT );
    }
    
    @JsonProperty( HYPERDEX_COORDINATORS_PROPERTY ) 
    public List<String> getCassandraSeedNodes() {
        return coordinators;
    }
    
    @JsonProperty( HYPERDEX_SPACE_PROPERTY )
    public String getKeyspace() {
        return keyspace;
    }
    
    @JsonProperty( HYPERDEX_TOLERATE_FAILURES_PROPERTY )
    public int getReplicationFactor() {
        return replicationFactor;
    }

}
