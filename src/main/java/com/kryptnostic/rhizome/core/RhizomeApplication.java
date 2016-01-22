package com.kryptnostic.rhizome.core;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

@ApplicationPath( "rhizome-jersey" )
public class RhizomeApplication extends ResourceConfig {

    public RhizomeApplication() {

        // eventually this should go to our rhizome app test
        // in case youre a fan of jersey
        register( RequestContextFilter.class );
    }
}
