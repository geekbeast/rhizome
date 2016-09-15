package com.kryptnostic.rhizome.pods;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.spark.SparkConfiguration;

@Configuration
@Profile( SparkPod.SPARK_PROFILE )
@Import( CassandraPod.class )
public class SparkPod {
    public static final String   SPARK_PROFILE = "spark";

    @Inject
    private RhizomeConfiguration rhizomeConfiguration;

    @Bean
    public SparkConf sparkConf() {
        Optional<SparkConfiguration> maybeSparkConfiguration = rhizomeConfiguration.getSparkConfiguration();
        Optional<CassandraConfiguration> maybeCassandraConfiguration = rhizomeConfiguration.getCassandraConfiguration();
        if ( maybeSparkConfiguration.isPresent() && maybeCassandraConfiguration.isPresent() ) {
            SparkConfiguration sparkConfiguration = maybeSparkConfiguration.get();
            CassandraConfiguration cassandraConfiguration = maybeCassandraConfiguration.get();
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
                    .set( "spark.cassandra.connection.host", cassandraConfiguration.getCassandraSeedNodes().stream()
                            .map( host -> host.getHostAddress() ).collect( Collectors.joining( "," ) ) )
                    .set( "spark.cassandra.connection.port", Integer.toString( 9042 ) )
                    .setJars( sparkConfiguration.getJarLocations() );
        }
        return null;

    }

    @Bean
    public SparkSession sparkSession() {
        SparkConf sc = sparkConf();
        return sc == null ? null : SparkSession.builder().config( sc ).getOrCreate();
    }

}
