package com.geekbeast.rhizome.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * @author Matthew Tamayo-Rios
 */
public class RhizomeConfiguration implements Configuration {
    private static final long serialVersionUID = -8444209890618166001L;

    protected static ConfigurationKey key = new SimpleConfigurationKey( "rhizome.yaml" );
    
    protected static final String PERSISTENCE_ENABLED_PROPERTY = "enable-persistence";
    protected static final String CASSANDRA_CONFIGURATION_PROPERTY = "cassandra";
    protected static final String GRAPHITE_CONFIGURATION_PROPERTY = "graphite";
    protected static final String HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY = "hazelcast";
    protected static final boolean PERSISTENCE_ENABLED_DEFAULT = true;
    
    protected final Logger logger = LoggerFactory.getLogger( getClass() );
    protected final boolean persistData;
    protected final Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration; 
    protected final Optional<GraphiteConfiguration> graphiteConfiguration;
    protected final Optional<CassandraConfiguration> cassandraConfiguration;

    @JsonCreator
    public RhizomeConfiguration(
            @JsonProperty( PERSISTENCE_ENABLED_PROPERTY ) boolean persistData ,
            @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY ) Optional<CassandraConfiguration> cassandraConfiguration ,
            @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY ) Optional<GraphiteConfiguration> graphiteConfiguration ,
            @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY ) Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration 
            ) {

        this.persistData = Objects.firstNonNull( persistData , PERSISTENCE_ENABLED_DEFAULT );
        this.cassandraConfiguration = cassandraConfiguration;
        this.graphiteConfiguration = graphiteConfiguration;
        this.hazelcastSessionFilterConfiguration = hazelcastSessionFilterConfiguration;
    }



    @Override
    public String toString() {
        return "HeimdallConfiguration [persistData=" + persistData
                + ", cassandraConfiguration=" + cassandraConfiguration
                + ", graphiteConfiguration=" + graphiteConfiguration + "]";
    }
    
    
    @JsonProperty( PERSISTENCE_ENABLED_PROPERTY )
    public boolean isPersistenceEnabled() {
        return persistData;
    }
    
    @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY )
    public Optional<CassandraConfiguration> getCassandraConfiguration() {
        return cassandraConfiguration;
    }
    
    @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY )
    public Optional<GraphiteConfiguration> getGraphiteConfiguration() {
        return graphiteConfiguration;
    }

    @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY ) 
    public Optional<HazelcastSessionFilterConfiguration> getHazelcastSessionFilterConfiguration() {
        return hazelcastSessionFilterConfiguration;
    }
    public static ConfigurationKey key() {
        return key;
    }
    
    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key;
    }
}
