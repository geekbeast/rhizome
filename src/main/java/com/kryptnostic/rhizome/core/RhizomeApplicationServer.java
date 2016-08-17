package com.kryptnostic.rhizome.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.kryptnostic.rhizome.pods.AsyncPod;
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.HazelcastPod;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;

public class RhizomeApplicationServer {
    private final AnnotationConfigApplicationContext context;

    public RhizomeApplicationServer() {
        this(
                new Class<?>[] { RegistryBasedHazelcastInstanceConfigurationPod.class, HazelcastPod.class,
                        AsyncPod.class, ConfigurationPod.class } );
    }

    public RhizomeApplicationServer( Class<?>... pods ) {
        this.context = new AnnotationConfigApplicationContext();
        context.register( pods );
    }

    public void intercrop( Class<?>... pods ) {
        context.register( pods );
    }

    public void sprout( String... activeProfiles ) {
        context.getEnvironment().setActiveProfiles( activeProfiles );
        context.refresh();
    }

    public void plowUnder() {
        context.close();
    }
    
    public AnnotationConfigApplicationContext getContext() {
        return context;
    }
}
