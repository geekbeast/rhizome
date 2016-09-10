package com.kryptnostic.rhizome.pods;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeCodec;
import com.google.common.annotations.VisibleForTesting;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfigurations;
import com.kryptnostic.rhizome.configuration.cassandra.Clusters;
import com.kryptnostic.rhizome.configuration.cassandra.Sessions;

import jersey.repackaged.com.google.common.collect.Maps;

@Configuration
@Profile( CassandraPod.CASSANDRA_PROFILE )
@Import( ConfigurationPod.class )
public class CassandraPod {
    public static final String   CASSANDRA_PROFILE = "cassandra";
    private static final Logger  logger            = LoggerFactory.getLogger( CassandraPod.class );

    @Inject
    private RhizomeConfiguration configuration;

    @Autowired(
        required = false )
    private Set<TypeCodec<?>>    codecs;

    @Bean
    public CassandraConfiguration cassandraConfiguration() {
        if ( !configuration.getCassandraConfiguration().isPresent() ) {
            throw new RuntimeException(
                    "Cassandra configuration is missing. Please add a cassandra configuration to rhizome.yaml" );
        }
        CassandraConfiguration cassandraConfiguration = configuration.getCassandraConfiguration().get();
        if ( cassandraConfiguration == null ) {
            logger.error(
                    "Seed nodes not found in cassandra configuration. Please add seed nodes to cassandra configuration block in rhizome.yaml" );
        }
        return cassandraConfiguration;
    }

    @Bean
    public Session session() {
        return cluster().newSession();
    }

    @Bean
    public CodecRegistry codecRegistry() {
        // TODO: Explicitly construct default instance codecs so that not all sessions end up having all codecs. P2
        CodecRegistry registry = CodecRegistry.DEFAULT_INSTANCE;
        if ( codecs != null ) {
            registry.register( codecs );
        }
        return registry;
    }

    @Bean
    public Cluster cluster() {
        return new Cluster.Builder()
                .withCompression( cassandraConfiguration().getCompression() )
                .withPoolingOptions( getPoolingOptions() )
                .withProtocolVersion( ProtocolVersion.NEWEST_SUPPORTED )
                .addContactPoints( cassandraConfiguration().getCassandraSeedNodes() )
                .withCodecRegistry( codecRegistry() )
                .build();
    }

    @Bean
    public Clusters clusters() {
        return new Clusters( Maps.transformValues( getConfigurations(),
                cassandraConfiguration -> new Cluster.Builder()
                        .withCompression( cassandraConfiguration.getCompression() )
                        .withPoolingOptions( getPoolingOptions() )
                        .withProtocolVersion( ProtocolVersion.NEWEST_SUPPORTED )
                        .addContactPoints( cassandraConfiguration.getCassandraSeedNodes() )
                        .withCodecRegistry( codecRegistry() )
                        .build() ) );
    }

    @Bean
    public Sessions sessions() {
        return new Sessions( Maps.transformValues( clusters(), cluster -> cluster.newSession() ) );
    }

    private static PoolingOptions getPoolingOptions() {
        PoolingOptions poolingOptions = new PoolingOptions();
        return poolingOptions;
    }

    @VisibleForTesting
    public void setRhizomeConfig( RhizomeConfiguration config ) {
        this.configuration = config;
    }

    @VisibleForTesting
    public void setCodecs( Set<TypeCodec<?>> codecs ) {
        this.codecs = codecs;
    }

    private CassandraConfigurations getConfigurations() {
        return configuration.getCassandraConfigurations().or( new CassandraConfigurations() );
    }
}
