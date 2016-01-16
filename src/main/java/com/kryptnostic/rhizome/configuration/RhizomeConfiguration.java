package com.kryptnostic.rhizome.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.graphite.GraphiteConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastSessionFilterConfiguration;
import com.kryptnostic.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.kryptnostic.rhizome.configuration.rethinkdb.RethinkDbConfiguration;

/**
 * @author Matthew Tamayo-Rios
 */
public class RhizomeConfiguration implements Configuration {
    private static final long                                     serialVersionUID                                = -8444209890618166001L;

    protected static ConfigurationKey                             key                                             = new SimpleConfigurationKey(
                                                                                                                          "rhizome.yaml" );

    protected static final String                                 PERSISTENCE_ENABLED_PROPERTY                    = "enable-persistence";
    protected static final String                                 SESSION_CLUSTERING_ENABLED_PROPERTY             = "session-clustering-enabled";
    protected static final String                                 CASSANDRA_CONFIGURATION_PROPERTY                = "cassandra";
    protected static final String                                 HYPERDEX_CONFIGURATION_PROPERTY                 = "hyperdex";
    protected static final String                                 GRAPHITE_CONFIGURATION_PROPERTY                 = "graphite";
    protected static final String                                 HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY = "hazelcast-session-filter";
    protected static final String                                 RETHINKDB_CONFIGURATION_PROPERTY                = "rethinkdb";
    protected static final String                                 HAZELCAST_CONFIGURATION_PROPERTY                = "hazelcast";
    protected static final boolean                                PERSISTENCE_ENABLED_DEFAULT                     = true;
    protected static final boolean                                SESSION_CLUSTERING_ENABLED_DEFAULT              = false;

    protected final Logger                                        logger                                          = LoggerFactory
                                                                                                                          .getLogger( getClass() );
    protected final boolean                                       persistData;
    protected final boolean                                       sessionClusteringEnabled;
    protected final Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration;
    protected final Optional<GraphiteConfiguration>               graphiteConfiguration;
    protected final Optional<CassandraConfiguration>              cassandraConfiguration;
    protected final Optional<HazelcastConfiguration>              hazelcastConfiguration;
    protected final Optional<HyperdexConfiguration>               hyperdexConfiguration;
    protected final Optional<RethinkDbConfiguration>              rethinkDbConfiguration;

    @JsonCreator
    public RhizomeConfiguration(
            @JsonProperty( PERSISTENCE_ENABLED_PROPERTY ) Optional<Boolean> persistData,
            @JsonProperty( SESSION_CLUSTERING_ENABLED_PROPERTY ) Optional<Boolean> sessionClusteringEnabled,
            @JsonProperty( RETHINKDB_CONFIGURATION_PROPERTY ) Optional<RethinkDbConfiguration> rethinkDbConfiguration,
            @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY ) Optional<CassandraConfiguration> cassandraConfiguration,
            @JsonProperty( HYPERDEX_CONFIGURATION_PROPERTY ) Optional<HyperdexConfiguration> hyperdexConfiguration,
            @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY ) Optional<GraphiteConfiguration> graphiteConfiguration,
            @JsonProperty( HAZELCAST_CONFIGURATION_PROPERTY ) Optional<HazelcastConfiguration> hazelcastConfiguration,
            @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY ) Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration ) {

        this.persistData = persistData.or( PERSISTENCE_ENABLED_DEFAULT );
        this.sessionClusteringEnabled = sessionClusteringEnabled.or( SESSION_CLUSTERING_ENABLED_DEFAULT );
        this.cassandraConfiguration = cassandraConfiguration;
        this.rethinkDbConfiguration = rethinkDbConfiguration;
        this.hyperdexConfiguration = hyperdexConfiguration;
        this.graphiteConfiguration = graphiteConfiguration;
        this.hazelcastConfiguration = hazelcastConfiguration;
        this.hazelcastSessionFilterConfiguration = hazelcastSessionFilterConfiguration;
    }

    @Override
    public String toString() {
        return "RhizomeConfiguration [persistData=" + persistData
                + ", hazelcastSessionFilterConfiguration=" + hazelcastSessionFilterConfiguration
                + ", graphiteConfiguration=" + graphiteConfiguration
                + ", cassandraConfiguration=" + cassandraConfiguration
                + ", rethinkDbConfiguration=" + rethinkDbConfiguration + "]";

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

    @JsonProperty( RETHINKDB_CONFIGURATION_PROPERTY )
    public Optional<RethinkDbConfiguration> getRethinkDbConfiguration() {
        return rethinkDbConfiguration;
    }

    @JsonProperty( HYPERDEX_CONFIGURATION_PROPERTY )
    public Optional<HyperdexConfiguration> getHyperdexConfiguration() {
        return hyperdexConfiguration;
    }

    @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY )
    public Optional<GraphiteConfiguration> getGraphiteConfiguration() {
        return graphiteConfiguration;
    }

    @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY )
    public Optional<HazelcastSessionFilterConfiguration> getHazelcastSessionFilterConfiguration() {
        return hazelcastSessionFilterConfiguration;
    }

    @JsonProperty( HAZELCAST_CONFIGURATION_PROPERTY )
    public Optional<HazelcastConfiguration> getHazelcastConfiguration() {
        return hazelcastConfiguration;
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