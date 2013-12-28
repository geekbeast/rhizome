package com.geekbeast.rhizome.core;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.geekbeast.rhizome.controllers.HealthCheckResource;

@ApplicationPath("rhizome-jersey")
public class RhizomeApplication extends ResourceConfig {
    
    public RhizomeApplication() {
        register( RequestContextFilter.class );
        register( HealthCheckResource.class );
    }
}
