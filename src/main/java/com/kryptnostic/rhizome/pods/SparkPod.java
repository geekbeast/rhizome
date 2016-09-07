package com.kryptnostic.rhizome.pods;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.spark.SparkConf;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.configuration.spark.SparkConfiguration;

@Configuration
@Profile( SparkPod.SPARK_PROFILE )
@Import( CassandraPod.class )
public class SparkPod {
    public static final String     SPARK_PROFILE = "spark";

    @Inject
    private SparkConfiguration     sparkConfiguration;

    @Inject
    private CassandraConfiguration cassandraConfiguration;

    @Bean
    public SparkConf sparkConf() {
        StringBuilder sparkMasterUrlBuilder = new StringBuilder( "spark://" );
        String sparkMastersAsString = sparkConfiguration.getSparkMasters().stream()
                .collect( Collectors.joining( "," ) );
        sparkMasterUrlBuilder.append( sparkMastersAsString );
        return new SparkConf()
                .setMaster( sparkMasterUrlBuilder.toString() )
                .set( "spark.cassandra.connection.host", cassandraConfiguration.getCassandraSeedNodes().stream()
                        .map( host -> host.getHostAddress() ).collect( Collectors.joining( "," ) ) )
                .set( "spark.cassandra.connection.port", Integer.toString( 9042 ) );
    }
}
