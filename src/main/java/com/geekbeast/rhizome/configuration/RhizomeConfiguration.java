package com.geekbeast.rhizome.configuration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.geekbeast.rhizome.configuration.graphite.GraphiteConfiguration;
import com.geekbeast.rhizome.configuration.hazelcast.HazelcastSessionFilterConfiguration;
import com.geekbeast.rhizome.configuration.servlets.DispatcherServletConfiguration;
import com.geekbeast.rhizome.configuration.servlets.JerseyServletConfiguration;
import com.google.common.base.Optional;

/**
 * @author Matthew Tamayo-Rios
 */
public class RhizomeConfiguration implements Configuration {
    private static final long serialVersionUID = -8444209890618166001L;

    protected static ConfigurationKey key = new SimpleConfigurationKey( "rhizome.yaml" );
    
    protected static final String PERSISTENCE_ENABLED_PROPERTY = "enable-persistence";
    protected static final String SESSION_CLUSTERING_ENABLED_PROPERTY = "session-clustering-enabled";
    protected static final String CASSANDRA_CONFIGURATION_PROPERTY = "cassandra";
    protected static final String GRAPHITE_CONFIGURATION_PROPERTY = "graphite";
    protected static final String HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY = "hazelcast";
    protected static final String DISPATCHER_SERVLETS_PROPERTY = "dispatcher-servlets";
    protected static final String JERSEY_SERVLETS_PROPERTY = "jersey-servlets";
    protected static final boolean PERSISTENCE_ENABLED_DEFAULT = true;
    protected static final boolean SESSION_CLUSTERING_ENABLED_DEFAULT = true;
    
    protected final Logger logger = LoggerFactory.getLogger( getClass() );
    protected final boolean persistData;
    protected final boolean sessionClusteringEnabled;
    protected final Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration; 
    protected final Optional<GraphiteConfiguration> graphiteConfiguration;
    protected final Optional<CassandraConfiguration> cassandraConfiguration;
    protected final Optional<List<DispatcherServletConfiguration>> dispatcherServlets;
    protected final Optional<List<JerseyServletConfiguration>> jerseyServlets;
    

    @JsonCreator
    public RhizomeConfiguration(
            @JsonProperty( PERSISTENCE_ENABLED_PROPERTY ) Optional<Boolean> persistData ,
            @JsonProperty( SESSION_CLUSTERING_ENABLED_PROPERTY ) Optional<Boolean> sessionClusteringEnabled ,
            @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY ) Optional<CassandraConfiguration> cassandraConfiguration ,
            @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY ) Optional<GraphiteConfiguration> graphiteConfiguration ,
            @JsonProperty( DISPATCHER_SERVLETS_PROPERTY ) Optional<List<DispatcherServletConfiguration>> dispatcherServlets,
            @JsonProperty( JERSEY_SERVLETS_PROPERTY ) Optional<List<JerseyServletConfiguration>> jerseyServlets,
            @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY ) Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration 
            ) {

        this.persistData = persistData.or( PERSISTENCE_ENABLED_DEFAULT );
        this.sessionClusteringEnabled = sessionClusteringEnabled.or( SESSION_CLUSTERING_ENABLED_DEFAULT );
        this.cassandraConfiguration = cassandraConfiguration;
        this.graphiteConfiguration = graphiteConfiguration;
        this.dispatcherServlets = dispatcherServlets;
        this.jerseyServlets = jerseyServlets;
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
    
    @JsonProperty( SESSION_CLUSTERING_ENABLED_PROPERTY )
    public boolean isSessionClusteringEnabled() {
        return sessionClusteringEnabled;
    }
    
    @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY )
    public Optional<CassandraConfiguration> getCassandraConfiguration() {
        return cassandraConfiguration;
    }
    
    @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY )
    public Optional<GraphiteConfiguration> getGraphiteConfiguration() {
        return graphiteConfiguration;
    }
    
    @JsonProperty( DISPATCHER_SERVLETS_PROPERTY )
    public Optional<List<DispatcherServletConfiguration>> getDispatcherServletConfigurations() {
        return dispatcherServlets;
    }
    
    @JsonProperty( JERSEY_SERVLETS_PROPERTY )
    public Optional<List<DispatcherServletConfiguration>> getJerseyServletConfigurations() {
        return dispatcherServlets;
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
