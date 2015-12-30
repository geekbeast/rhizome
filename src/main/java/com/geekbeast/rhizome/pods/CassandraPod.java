package com.geekbeast.rhizome.pods;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;

@Configuration
@Profile( "cassandra" )
public class CassandraPod {
    private static final Logger  logger = LoggerFactory.getLogger( CassandraPod.class );

    @Inject
    private RhizomeConfiguration configuration;

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
    public Cluster getCluster() {
        return new Cluster.Builder()
                .withPoolingOptions( getPoolingOptions() )
                .withProtocolVersion( ProtocolVersion.V3 )
                .addContactPoints( cassandraConfiguration().getCassandraSeedNodes() )
                .build();
    }

    private static PoolingOptions getPoolingOptions() {
        PoolingOptions poolingOptions = new PoolingOptions();
        return poolingOptions;
    }
}
