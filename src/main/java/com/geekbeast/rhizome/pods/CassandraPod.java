package com.geekbeast.rhizome.pods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.geekbeast.rhizome.configuration.ConfigurationConstants.HZ;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.geekbeast.rhizome.pods.CassandraMapStoreFactory.CassandraMapStoreFactoryBuilder;
import com.kryptnostic.rhizome.cassandra.BaseCassandraMapStore;

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
        return new CassandraMapStoreFactoryBuilder()
                .withTable( HZ.MAPS.CONFIGURATION )
                .withKeyspace( config.getKeyspace() )
                .withMapName( "kryptnostic" )
                .withConfiguration( config )
                .withCluster( cluster )
                .build()
                    .getMapstore( ConfigurationKey.class, String.class );

    }

}
