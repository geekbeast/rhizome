package com.geekbeast.rhizome.pods;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.graphite.GraphiteConfiguration;
import com.google.common.base.Objects;
import com.hazelcast.core.HazelcastInstance;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;

/**
 * @author Matthew Tamayo-Rios
 */
@Configuration
@EnableMetrics
public class MetricsPod implements MetricsConfigurer {
    private static final Logger logger = LoggerFactory.getLogger( MetricsPod.class );
    
    @Inject
    private RhizomeConfiguration config;
    
    @Bean
    public MetricRegistry globalMetricRegistry() {
        return new MetricRegistry();
    }
    
    @Bean
    public MetricRegistry serverMetricRegistry() {
        return new MetricRegistry();
    }
    
    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }
    
    @Bean 
    GraphiteReporter globalGraphiteReporter() {
        return GraphiteReporter.forRegistry( globalMetricRegistry() )
                .prefixedWith( getGlobalName() )
                .convertDurationsTo(TimeUnit.SECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build( globalGraphite() );
    }

    @Bean 
    GraphiteReporter serverGraphiteReporter() {
        return GraphiteReporter.forRegistry( serverMetricRegistry() )
                .prefixedWith( getHostName() )
                .convertDurationsTo(TimeUnit.SECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build( serverGraphite() );
    }

    @Override
    public void configureReporters( MetricRegistry registry ) {
        
    }

    @Override
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry();
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }
    
    //TODO: Configure global aggregation for graphite
    @Bean 
    public Graphite globalGraphite() {
        if( config.getGraphiteConfiguration().isPresent() ) {
            GraphiteConfiguration graphiteConfig = config.getGraphiteConfiguration().get();
            logger.info( "Initializing global graphite instance with at {}:{}" , 
                    graphiteConfig.getGraphiteHost() ,
                    graphiteConfig.getGraphitePort() );
            return new Graphite( new InetSocketAddress( graphiteConfig.getGraphiteHost() , graphiteConfig.getGraphitePort() ) );
        } else {
            return null;
        }
    }
    
    @Bean 
    public Graphite serverGraphite() {
        if( config.getGraphiteConfiguration().isPresent() ) {
            GraphiteConfiguration graphiteConfig = config.getGraphiteConfiguration().get();
            logger.info( "Initializing global graphite instance with at {}:{}" , 
                    graphiteConfig.getGraphiteHost() ,
                    graphiteConfig.getGraphitePort() );
            return new Graphite( new InetSocketAddress( graphiteConfig.getGraphiteHost() , graphiteConfig.getGraphitePort() ) );
        } else {
            return null;
        }
    }
    
    @PostConstruct
    protected void startGraphite() {
        globalGraphiteReporter().start( 10 , TimeUnit.SECONDS );
        serverGraphiteReporter().start( 10 , TimeUnit.SECONDS );
    }
    
    protected String getHostName() { 
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn( "Unable to determine hostname, default to Hazelcast UUID");
            return null;
        }
    }
    
    protected String getGlobalName() { 
        if( config.getGraphiteConfiguration().isPresent() ) {
            return config.getGraphiteConfiguration().get().getGraphiteGlobalPrefix();
        } else {
            return "global";
        }
    }
}
