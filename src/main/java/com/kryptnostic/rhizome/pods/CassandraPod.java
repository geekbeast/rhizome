package com.kryptnostic.rhizome.pods;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@Profile( "cassandra" )
public class CassandraPod {
    private static final Logger  logger = LoggerFactory.getLogger( CassandraPod.class );

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
    public Session sess() {
        return getCluster().newSession();
    }

    @Bean
    public CodecRegistry getCodecRegistry() {
        return new CodecRegistry().register( codecs );
    }

    @Bean
    public Cluster getCluster() {
        return new Cluster.Builder()
                .withCompression( cassandraConfiguration().getCompression() )
                .withPoolingOptions( getPoolingOptions() )
                .withProtocolVersion( ProtocolVersion.V3 )
                .addContactPoints( cassandraConfiguration().getCassandraSeedNodes() )
                .withCodecRegistry( getCodecRegistry() )
                .build();
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
}
