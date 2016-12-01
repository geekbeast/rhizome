package com.kryptnostic.rhizome.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.hazelcast.core.HazelcastInstance;
import com.kryptnostic.rhizome.hazelcast.serializers.RhizomeUtils.Pods;
import com.kryptnostic.rhizome.pods.AsyncPod;
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.HazelcastPod;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;

public class RhizomeApplicationServer {
    private final AnnotationConfigApplicationContext context     = new AnnotationConfigApplicationContext();;
    public static final Class<?>[]                   defaultPods = new Class<?>[] {
            RegistryBasedHazelcastInstanceConfigurationPod.class, HazelcastPod.class,
            AsyncPod.class, ConfigurationPod.class };

    public RhizomeApplicationServer( Class<?>... pods ) {
        this.context.register( Pods.concatenate( defaultPods, pods ) );
    }

    public void intercrop( Class<?>... pods ) {
        context.register( pods );
    }

    public void sprout( String... activeProfiles ) {
        for ( String activeProfile : activeProfiles ) {
            context.getEnvironment().addActiveProfile( activeProfile );
        }
        context.refresh();
        context.start();
    }

    public void plowUnder() {
        HazelcastInstance hazelcast = context.getBean( HazelcastInstance.class );
        if ( hazelcast != null ) {
            hazelcast.shutdown();
        }
        context.close();
    }

    public AnnotationConfigApplicationContext getContext() {
        return context;
    }
}
