package com.geekbeast.rhizome.pods;

import com.geekbeast.rhizome.configuration.ConfigurationConstants.Profiles;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.geekbeast.rhizome.configuration.service.ConfigurationService;

/**
 * The configuration pod is responsible for bootstrapping the initial environment. It sets up component scanning
 * <ul>
 * <li>Component Scanning</li>
 * <li>Configurations</li>
 * </ul>
 * By default it does not scan com.geekbeast.rhizome.pods. Each pod should be registered as required in the
 * {@code RhizomeService.initialize(...)} method.
 * 
 * @author Matthew Tamayo-Rios
 */
@Configuration
@Profile( Profiles.LOCAL_CONFIGURATION_PROFILE )
public class LocalConfigurationPod {
    private static final Logger               logger = LoggerFactory.getLogger( LocalConfigurationPod.class );
    private static final RhizomeConfiguration rhizomeConfiguration;
    private static final JettyConfiguration   jettyConfiguration;

    static {
        try {
            rhizomeConfiguration = ConfigurationService.StaticLoader.loadConfiguration( RhizomeConfiguration.class );
            jettyConfiguration = ConfigurationService.StaticLoader.loadConfiguration( JettyConfiguration.class );
        } catch ( Exception e ) {
            logger.error( "Error loading configuration!", e );
            throw new Error( "Configuration failure." );
        }
    }

    @Bean
    public RhizomeConfiguration rhizomeConfiguration() {
        return rhizomeConfiguration;
    }

    @Bean
    public JettyConfiguration jettyConfiguration() {
        return jettyConfiguration;
    }
}
