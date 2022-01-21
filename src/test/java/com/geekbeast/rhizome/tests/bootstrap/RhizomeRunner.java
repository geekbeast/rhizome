package com.geekbeast.rhizome.tests.bootstrap;

import com.geekbeast.rhizome.tests.authentication.Auth0SecurityTestPod;
import com.geekbeast.rhizome.tests.pods.DispatcherServletsPod;
import com.geekbeast.rhizome.core.Rhizome;
import com.geekbeast.rhizome.pods.ConfigurationLoaderPod;
import com.geekbeast.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import com.geekbeast.auth0.Auth0Pod;
import com.openlattice.authentication.AuthenticationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class RhizomeRunner {
    private static final Logger logger = LoggerFactory.getLogger( RhizomeRunner.class );
    public static void main(String[] args ) {
        logger.info("Token: {}", AuthenticationTest.authenticate().getCredentials());
        final var rhizome = new Rhizome(
                ConfigurationLoaderPod.class,
                Auth0Pod.class,
                Auth0SecurityTestPod.class,
                DispatcherServletsPod.class,
                RegistryBasedHazelcastInstanceConfigurationPod.class );
        rhizome.sprout();
        logger.info( "Successfully started Rhizome microservice." );
    }
}
