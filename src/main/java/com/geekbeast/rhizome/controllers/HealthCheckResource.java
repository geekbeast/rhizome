package com.geekbeast.rhizome.controllers;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry;

@Component
@Path("")
public class HealthCheckResource {
    private final HealthCheckRegistry healthCheckRegistry;
    
    @Inject
    public HealthCheckResource( HealthCheckRegistry healthCheckRegistry ) {
        this.healthCheckRegistry = healthCheckRegistry;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Result> getHealthChecks() {
        return healthCheckRegistry.runHealthChecks();
    }
}
