package com.geekbeast.rhizome.pods;

import java.net.InetAddress;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.geekbeast.rhizome.configuration.ConfigurationConstants.HZ;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.kryptnostic.rhizome.mapstores.cassandra.CassandraMapStoreFactory;

@Configuration
@Profile( "cassandra" )
@Import( { RegistryBasedMappersPod.class } )
public class CassandraPod {
    private static final Logger               logger        = LoggerFactory.getLogger( CassandraPod.class );
    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();

    @Inject
    RegistryBasedMappersPod mappers;

    @Bean
    public static CassandraConfiguration cassandraConfiguration() {
        if ( !configuration.getCassandraConfiguration().isPresent() ) {
            throw new RuntimeException( "Cassandra configuration is missing. Please add a cassandra configuration to rhizome.yaml" );
        }
        CassandraConfiguration cassandraConfiguration = configuration.getCassandraConfiguration().get();
        if ( cassandraConfiguration == null ) {
            logger.error(
                    "Seed nodes not found in cassandra configuration. Please add seed nodes to cassandra configuration block in rhizome.yaml" );
        }
        return cassandraConfiguration;
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
                .addContactPoints( cassandraConfiguration().getCassandraSeedNodes() )
                .build();
    }

    @Bean
    public TestableSelfRegisteringMapStore<ConfigurationKey, String> getConfigurationMapStore() {
        CassandraConfiguration config = cassandraConfiguration();
        Cluster cluster = getCluster();
        return new CassandraMapStoreFactory.Builder()
                .withConfiguration( config )
                .withSession( cluster.newSession() )
                .withMappers( mappers )
                .build()
                .build( ConfigurationKey.class, String.class )
                .withTableAndMapName( HZ.MAPS.CONFIGURATION )
                .build();
    }

}
