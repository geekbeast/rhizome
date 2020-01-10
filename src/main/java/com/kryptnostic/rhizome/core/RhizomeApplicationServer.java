package com.kryptnostic.rhizome.core;

import com.hazelcast.core.HazelcastInstance;
import com.kryptnostic.rhizome.hazelcast.serializers.RhizomeUtils.Pods;
import com.kryptnostic.rhizome.pods.AsyncPod;
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.HazelcastPod;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import com.kryptnostic.rhizome.startup.Requirement;
import com.openlattice.auth0.Auth0Pod;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kryptnostic.rhizome.core.Rhizome.shoot;
import static com.kryptnostic.rhizome.core.Rhizome.showBannerIfStarted;

public class RhizomeApplicationServer {
    private final        AnnotationConfigApplicationContext context        = new AnnotationConfigApplicationContext();
    private final        List<Class<?>>                     additionalPods = new ArrayList<>();
    public static final  Class<?>[]                         DEFAULT_PODS   = new Class<?>[] {
            AsyncPod.class,
            Auth0Pod.class,
            ConfigurationPod.class,
            HazelcastPod.class,
            RegistryBasedHazelcastInstanceConfigurationPod.class };

    public RhizomeApplicationServer( Class<?>... pods ) {
        this( DEFAULT_PODS, pods );
    }

    private RhizomeApplicationServer( Class<?>[] basePods, Class<?>... pods ) {
        this.context.register( Pods.concatenate( basePods, pods ) );
    }

    public void intercrop( Class<?>... pods ) {
        additionalPods.addAll( Arrays.asList( pods ) );
    }

    public void sprout( String... activeProfiles ) {
        shoot( context, activeProfiles );

        if ( additionalPods.size() > 0 ) {
            context.register( additionalPods.toArray( new Class<?>[] {} ) );
        }
        context.refresh();

        showBannerIfStarted( context );
    }

    public void plowUnder() {
        HazelcastInstance hazelcast = context.getBean( HazelcastInstance.class );
        if ( hazelcast != null ) {
            hazelcast.shutdown();
        }
        context.close();
    }

    public boolean isActive() {
        return context.isActive();
    }

    public AnnotationConfigApplicationContext getContext() {
        return context;
    }

    public static boolean startupRequirementsSatisfied( AnnotationConfigApplicationContext context ) {
        return context.getBeansOfType( Requirement.class )
                .values()
                .parallelStream()
                .allMatch( Requirement::isSatisfied );
    }
}
