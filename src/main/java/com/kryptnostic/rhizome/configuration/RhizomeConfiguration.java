package com.kryptnostic.rhizome.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.annotation.ReloadableConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfigurations;
import com.kryptnostic.rhizome.configuration.graphite.GraphiteConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastSessionFilterConfiguration;
import com.kryptnostic.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.kryptnostic.rhizome.configuration.spark.SparkConfiguration;

/**
 * @author Matthew Tamayo-Rios
 */
@ReloadableConfiguration(uri="rhizome.yaml")
public class RhizomeConfiguration implements Configuration {
    private static final long                                     serialVersionUID                                = -8444209890618166001L;

    protected static ConfigurationKey                             key                                             = new SimpleConfigurationKey(
                                                                                                                          "rhizome.yaml" );

    protected static final String                                 PERSISTENCE_ENABLED_PROPERTY                    = "enable-persistence";
    protected static final String                                 SESSION_CLUSTERING_ENABLED_PROPERTY             = "session-clustering-enabled";
    protected static final String                                 CORS_ACCESS_CONTROL_ALLOW_ORIGIN_URL            = "cors-access-control-allow-origin-url";
    protected static final String                                 CASSANDRA_CONFIGURATION_PROPERTY                = "cassandra";
    protected static final String                                 SPARK_CONFIGURATION_PROPERTY                    = "spark";
    protected static final String                                 CASSANDRA_CONFIGURATIONS_PROPERTY               = "cassandras";
    protected static final String                                 GRAPHITE_CONFIGURATION_PROPERTY                 = "graphite";
    protected static final String                                 HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY = "hazelcast-session-filter";
    protected static final String                                 RETHINKDB_CONFIGURATION_PROPERTY                = "rethinkdb";
    protected static final String                                 HAZELCAST_CONFIGURATION_PROPERTY                = "hazelcast";
    protected static final boolean                                PERSISTENCE_ENABLED_DEFAULT                     = true;
    protected static final boolean                                SESSION_CLUSTERING_ENABLED_DEFAULT              = false;

    protected final Logger                                        logger                                          = LoggerFactory
                                                                                                                          .getLogger(
                                                                                                                                  getClass() );
    protected final boolean                                       persistData;
    protected final boolean                                       sessionClusteringEnabled;
    protected final String                                        corsAccessControlAllowOriginUrl;
    protected final Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration;
    protected final Optional<GraphiteConfiguration>               graphiteConfiguration;
    @Deprecated
    protected final Optional<CassandraConfiguration>              cassandraConfiguration;
    protected final Optional<CassandraConfigurations>             cassandraConfigurations;
    protected final Optional<HazelcastConfiguration>              hazelcastConfiguration;
    protected final Optional<RethinkDbConfiguration>              rethinkDbConfiguration;
    protected final Optional<SparkConfiguration>                  sparkConfiguration;

    @JsonCreator
    public RhizomeConfiguration(
            @JsonProperty( PERSISTENCE_ENABLED_PROPERTY ) Optional<Boolean> persistData,
            @JsonProperty( SESSION_CLUSTERING_ENABLED_PROPERTY ) Optional<Boolean> sessionClusteringEnabled,
            @JsonProperty( CORS_ACCESS_CONTROL_ALLOW_ORIGIN_URL ) Optional<String> corsAccessControlAllowOriginUrl,
            @JsonProperty( RETHINKDB_CONFIGURATION_PROPERTY ) Optional<RethinkDbConfiguration> rethinkDbConfiguration,
            @Deprecated @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY ) Optional<CassandraConfiguration> cassandraConfiguration,
            @JsonProperty( CASSANDRA_CONFIGURATIONS_PROPERTY ) Optional<CassandraConfigurations> cassandraConfigurations,
            @JsonProperty( GRAPHITE_CONFIGURATION_PROPERTY ) Optional<GraphiteConfiguration> graphiteConfiguration,
            @JsonProperty( HAZELCAST_CONFIGURATION_PROPERTY ) Optional<HazelcastConfiguration> hazelcastConfiguration,
            @JsonProperty( HAZELCAST_SESSION_FILTER_CONFIGURATION_PROPERTY ) Optional<HazelcastSessionFilterConfiguration> hazelcastSessionFilterConfiguration,
            @JsonProperty( SPARK_CONFIGURATION_PROPERTY ) Optional<SparkConfiguration> sparkConfig) {
        this.persistData = persistData.or( PERSISTENCE_ENABLED_DEFAULT );
        this.sessionClusteringEnabled = sessionClusteringEnabled.or( SESSION_CLUSTERING_ENABLED_DEFAULT );
        this.corsAccessControlAllowOriginUrl = corsAccessControlAllowOriginUrl.or( "" );
        this.cassandraConfiguration = cassandraConfiguration;
        this.cassandraConfigurations = cassandraConfigurations;
        this.rethinkDbConfiguration = rethinkDbConfiguration;
        this.graphiteConfiguration = graphiteConfiguration;
        this.hazelcastConfiguration = hazelcastConfiguration;
        this.hazelcastSessionFilterConfiguration = hazelcastSessionFilterConfiguration;
        this.sparkConfiguration = sparkConfig;
    }

    @Override
    public String toString() {
        return "RhizomeConfiguration [persistData=" + persistData
                + ", hazelcastSessionFilterConfiguration=" + hazelcastSessionFilterConfiguration
                + ", graphiteConfiguration=" + graphiteConfiguration
                + ", cassandraConfigurations=" + cassandraConfigurations
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

    @JsonProperty( CORS_ACCESS_CONTROL_ALLOW_ORIGIN_URL )
    public String getCORSAccessControlAllowOriginUrl() {
        return corsAccessControlAllowOriginUrl;
    }

    @Deprecated
    @JsonProperty( CASSANDRA_CONFIGURATION_PROPERTY )
    public Optional<CassandraConfiguration> getCassandraConfiguration() {
        return cassandraConfiguration;
    }

    @JsonProperty( CASSANDRA_CONFIGURATIONS_PROPERTY )
    public Optional<CassandraConfigurations> getCassandraConfigurations() {
        return cassandraConfigurations;
    }

    @JsonProperty( RETHINKDB_CONFIGURATION_PROPERTY )
    public Optional<RethinkDbConfiguration> getRethinkDbConfiguration() {
        return rethinkDbConfiguration;
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

    @JsonProperty( SPARK_CONFIGURATION_PROPERTY )
    public Optional<SparkConfiguration> getSparkConfiguration() {
        return sparkConfiguration;
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
