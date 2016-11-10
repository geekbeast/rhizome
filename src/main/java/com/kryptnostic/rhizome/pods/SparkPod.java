package com.kryptnostic.rhizome.pods;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.spark.SparkConfiguration;

@Configuration
@Profile( SparkPod.SPARK_PROFILE )
@Import( CassandraPod.class )
public class SparkPod {
    public static final String   SPARK_PROFILE                         = "spark";
    private static final String  CASSANDRA_CONNECTION_FACTORY_PROPERTY = "spark.cassandra.connection.factory";
    private static Cluster       CLUSTER                               = null;

    public static String         CASSANDRA_CONNECTION_FACTORY_CLASS    = null;

    @Inject
    private RhizomeConfiguration rhizomeConfiguration;

    @Bean
    public SparkConf sparkConf() {
        Optional<SparkConfiguration> maybeSparkConfiguration = rhizomeConfiguration.getSparkConfiguration();
        Optional<CassandraConfiguration> maybeCassandraConfiguration = rhizomeConfiguration.getCassandraConfiguration();
        if ( maybeSparkConfiguration.isPresent() && maybeCassandraConfiguration.isPresent() ) {
            SparkConfiguration sparkConfiguration = maybeSparkConfiguration.get();
            CassandraConfiguration cassandraConfiguration = maybeCassandraConfiguration.get();
            CLUSTER = CassandraPod.clusterBuilder( cassandraConfiguration )
                    .withCodecRegistry( CodecRegistry.DEFAULT_INSTANCE ).build();
            StringBuilder sparkMasterUrlBuilder;
            if ( sparkConfiguration.isLocal() ) {
                sparkMasterUrlBuilder = new StringBuilder( sparkConfiguration.getSparkMasters().iterator().next() );
            } else {
                sparkMasterUrlBuilder = new StringBuilder( "spark://" );
                String sparkMastersAsString = sparkConfiguration.getSparkMasters().stream()
                        .map( master -> master + ":" + Integer.toString( sparkConfiguration.getSparkPort() ) )
                        .collect( Collectors.joining( "," ) );
                sparkMasterUrlBuilder.append( sparkMastersAsString );
            }

            return new SparkConf()
                    .setMaster( sparkMasterUrlBuilder.toString() )
                    .setAppName( sparkConfiguration.getAppName() )
                    .set( "spark.sql.warehouse.dir", "file:///" + sparkConfiguration.getWorkingDirectory() )
                    .set( "spark.cassandra.connection.host", cassandraConfiguration.getCassandraSeedNodes().stream()
                            .map( host -> host.getHostAddress() ).collect( Collectors.joining( "," ) ) )
                    .set( "spark.cassandra.connection.port", Integer.toString( 9042 ) )
                    .set( "spark.cassandra.connection.ssl.enabled",
                            String.valueOf( cassandraConfiguration.isSslEnabled() ) )
                    .setJars( sparkConfiguration.getJarLocations() );
        }
        return null;
    }

    @Bean
    public SparkSession sparkSession() {
        SparkConf sc = sparkConf();
        if ( StringUtils.isNotBlank( CASSANDRA_CONNECTION_FACTORY_CLASS ) ) {
            sc.set( CASSANDRA_CONNECTION_FACTORY_PROPERTY, CASSANDRA_CONNECTION_FACTORY_CLASS );
        }
        return sc == null ? null : SparkSession.builder().config( sc ).getOrCreate();
    }

    /**
     * This is hack to allow bootstrapping cluster from cassandra configuration.
     * 
     * @return A cluster instance as described by the default CassandraConfiguration. Will return null if called before
     *         spring invokes {@code SparkPod#sparkConf()}.
     */
    public static Cluster getCluster() {
        return CLUSTER;
    }

}
