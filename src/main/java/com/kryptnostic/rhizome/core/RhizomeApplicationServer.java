package com.kryptnostic.rhizome.core;

import com.kryptnostic.rhizome.startup.Requirement;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.hazelcast.core.HazelcastInstance;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.hazelcast.serializers.RhizomeUtils.Pods;
import com.kryptnostic.rhizome.pods.AsyncPod;
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.HazelcastPod;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;

public class RhizomeApplicationServer {
    private final AnnotationConfigApplicationContext context     = new AnnotationConfigApplicationContext();;
    public static final Class<?>[] DEFAULT_PODS = new Class<?>[] {
            RegistryBasedHazelcastInstanceConfigurationPod.class, HazelcastPod.class,
            AsyncPod.class, ConfigurationPod.class };

    public RhizomeApplicationServer( Class<?>... pods ) {
        this( DEFAULT_PODS , pods );
    }

    private RhizomeApplicationServer( Class<?>[] basePods, Class<?>... pods ) {
        this.context.register( Pods.concatenate( basePods, pods ) );
    }

    public void intercrop( Class<?>... pods ) {
        context.register( pods );
    }

    public void sprout( String... activeProfiles ) {
        boolean awsProfile = false;
        boolean localProfile = false;
        for ( String profile : activeProfiles ) {
            if ( StringUtils.equals( Profiles.AWS_CONFIGURATION_PROFILE, profile ) ) {
                awsProfile = true;
            }

            if ( StringUtils.equals( Profiles.LOCAL_CONFIGURATION_PROFILE, profile ) ) {
                localProfile = true;
            }

            context.getEnvironment().addActiveProfile( profile );
        }

        if ( !awsProfile && !localProfile ) {
            context.getEnvironment().addActiveProfile( Profiles.LOCAL_CONFIGURATION_PROFILE );
        }
        context.refresh();
        if( context.isActive() && startupRequirementsSatisfied( context ) ) {
            Rhizome.showBanner();
        }
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
