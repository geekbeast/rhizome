package com.geekbeast.rhizome.configuration.service;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheck;
import com.geekbeast.rhizome.configuration.containers.JettyConfiguration;

@Component
public class ConfigurationServicesHealthCheck extends HealthCheck{
    @Inject
    private ConfigurationService service;

    //TODO: Use a configuration that isn't always statically available.
    @Override
    protected Result check() throws Exception {
        if ( service != null && service.getConfiguration( JettyConfiguration.class ) != null ) {
            return Result.healthy("Persistence service is function normally.");
        } else {
            return Result.unhealthy("Peristence service is failing.");
        }
    }
    
}
