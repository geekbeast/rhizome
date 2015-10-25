package com.geekbeast.rhizome.pods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.geekbeast.rhizome.configuration.ConfigurationConstants;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.cassandra.BaseCassandraMapStore;
import com.kryptnostic.rhizome.cassandra.SimpleCassandraMapper;
import com.kryptnostic.rhizome.mappers.keys.ConfigurationKeyMapper;

@Configuration
@Profile( "cassandra" )
public class CassandraPod {
    private static final Logger               logger        = LoggerFactory.getLogger( CassandraPod.class );
    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();

    @Bean
    public static CassandraConfiguration cassandraConfiguration() {
        if ( configuration.getCassandraConfiguration().isPresent() ) {
            return configuration.getCassandraConfiguration().get();
        }
        logger.error( "Cassandra configuration is missing. Please add a cassandra configuration to rhizome.yaml" );
        return null;
    }

    public static PoolingOptions getPoolingOptions() {
        PoolingOptions poolingOptions = new PoolingOptions();
        return poolingOptions;
    }

    @Bean
    public static Cluster getCluster() {
        return new Cluster.Builder()
            .withPoolingOptions( getPoolingOptions() )
            .withProtocolVersion( ProtocolVersion.V3 )
            .withClusterName( "YELL AT DREW" )
            .addContactPoints( cassandraConfiguration().getCassandraSeedNodes() )
            .build();
    }

    @Bean
    public static BaseCassandraMapStore<ConfigurationKey, String> getConfigurationMapStore() {
        CassandraConfiguration config = cassandraConfiguration();
        Cluster cluster = getCluster();
        return new BaseCassandraMapStore<ConfigurationKey, String>(
                config.getKeyspace(),
                ConfigurationConstants.HZ.MAPS.CONFIGURATION,
                new ConfigurationKeyMapper(),
                new SimpleCassandraMapper<String>( String.class ),
                config.getReplicationFactor(),
                cluster.newSession(),
                cluster );
    }

}
