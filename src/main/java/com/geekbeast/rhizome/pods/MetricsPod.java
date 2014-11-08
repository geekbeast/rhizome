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
import org.springframework.context.annotation.Import;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.graphite.GraphiteConfiguration;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;

/**
 * @author Matthew Tamayo-Rios
 */
@Configuration
@EnableMetrics(
    proxyTargetClass = true )
@Import(
    value = AsyncPod.class )
public class MetricsPod implements MetricsConfigurer {
    private static final Logger              logger              = LoggerFactory.getLogger( MetricsPod.class );
    // private static final MetricRegistry globalMetricRegistry = new MetricRegistry();
    private static final MetricRegistry      metricRegistry      = new MetricRegistry();
    private static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Inject
    private RhizomeConfiguration             config;

    @Bean
    GraphiteReporter globalGraphiteReporter() {
        return GraphiteReporter.forRegistry( metricRegistry ).prefixedWith( getGlobalName() )
                .convertDurationsTo( TimeUnit.MILLISECONDS ).convertRatesTo( TimeUnit.SECONDS )
                .build( globalGraphite() );
    }

    @Bean
    @Timed
    GraphiteReporter serverGraphiteReporter() {
        return GraphiteReporter.forRegistry( metricRegistry ).prefixedWith( getHostName() )
                .convertDurationsTo( TimeUnit.MILLISECONDS ).convertRatesTo( TimeUnit.SECONDS )
                .build( serverGraphite() );
    }

    @Override
    public void configureReporters( MetricRegistry registry ) {

    }

    @Override
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    // TODO: Configure global aggregation for graphite
    @Bean
    public Graphite globalGraphite() {
        if ( config.getGraphiteConfiguration().isPresent() ) {
            GraphiteConfiguration graphiteConfig = config.getGraphiteConfiguration().get();
            logger.info(
                    "Initializing global graphite instance with at {}:{}",
                    graphiteConfig.getGraphiteHost(),
                    graphiteConfig.getGraphitePort() );
            return new Graphite( new InetSocketAddress(
                    graphiteConfig.getGraphiteHost(),
                    graphiteConfig.getGraphitePort() ) );
        } else {
            return null;
        }
    }

    @Bean
    public Graphite serverGraphite() {
        if ( config.getGraphiteConfiguration().isPresent() ) {
            GraphiteConfiguration graphiteConfig = config.getGraphiteConfiguration().get();
            logger.info(
                    "Initializing server graphite instance with at {}:{}",
                    graphiteConfig.getGraphiteHost(),
                    graphiteConfig.getGraphitePort() );
            return new Graphite( new InetSocketAddress(
                    graphiteConfig.getGraphiteHost(),
                    graphiteConfig.getGraphitePort() ) );
        } else {
            return null;
        }
    }

    @PostConstruct
    protected void startGraphite() {
        globalGraphiteReporter().start( 10, TimeUnit.SECONDS );
        serverGraphiteReporter().start( 10, TimeUnit.SECONDS );
    }

    protected String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch ( UnknownHostException e ) {
            logger.warn( "Unable to determine hostname, default to Hazelcast UUID" );
            return null;
        }
    }

    protected String getGlobalName() {
        if ( config.getGraphiteConfiguration().isPresent() ) {
            return config.getGraphiteConfiguration().get().getGraphiteGlobalPrefix();
        } else {
            return "global";
        }
    }
}
