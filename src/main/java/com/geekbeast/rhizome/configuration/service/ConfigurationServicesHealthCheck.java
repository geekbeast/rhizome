package com.geekbeast.rhizome.configuration.service;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheck;
import com.geekbeast.rhizome.configuration.containers.JettyConfiguration;

@Component
public class ConfigurationServicesHealthCheck extends HealthCheck{
    @Inject
    private RhizomeConfigurationService service;

    @Override
    protected Result check() throws Exception {
        if ( service != null && service.loadConfiguration( JettyConfiguration.class ) != null ) {
            return Result.healthy("Persistence service is function normally.");
        } else {
            return Result.unhealthy("Peristence service is failing.");
        }
    }
    
}
