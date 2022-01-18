package com.geekbeast.rhizome.configuration.service;

import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import javax.inject.Inject;

import com.codahale.metrics.health.HealthCheck;

public class ConfigurationServicesHealthCheck extends HealthCheck {
    @Inject
    private ConfigurationService service;

    // TODO: Use a configuration that isn't always statically available.
    @Override
    protected Result check() throws Exception {
        if ( service != null && service.getConfiguration( JettyConfiguration.class ) != null ) {
            return Result.healthy( "Persistence service is function normally." );
        }
        return Result.unhealthy( "Peristence service is failing." );
    }

}
