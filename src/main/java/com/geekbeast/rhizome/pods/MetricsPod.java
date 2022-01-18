package com.geekbeast.rhizome.pods;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Matthew Tamayo-Rios
 */
@Configuration
@EnableMetrics( proxyTargetClass = true )
@Import( { AsyncPod.class, ConfigurationPod.class } )
public class MetricsPod implements MetricsConfigurer {

    private static final Logger              logger              = LoggerFactory.getLogger( MetricsPod.class );
    private static final MetricRegistry      metricRegistry      = new MetricRegistry();
    private static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Inject
    private RhizomeConfiguration config;

    @Override
    public void configureReporters( MetricRegistry registry ) {

        // https://github.com/prometheus/client_java/issues/101#issuecomment-275143650
        CollectorRegistry.defaultRegistry.register( new DropwizardExports( registry ) );
    }

    @Override
    @Bean
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    @Override
    @Bean
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    protected String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch ( UnknownHostException e ) {
            logger.warn( "Unable to determine hostname, default to Hazelcast UUID", e );
            return null;
        }
    }

    protected String getGlobalName() {
        if ( config.getGraphiteConfiguration().isPresent() ) {
            return config.getGraphiteConfiguration().get().getGraphiteGlobalPrefix();
        }
        return "global";
    }
}
