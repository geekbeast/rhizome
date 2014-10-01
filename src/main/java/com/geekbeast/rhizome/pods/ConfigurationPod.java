package com.geekbeast.rhizome.pods;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.configuration.service.ConfigurationService;

/**
 * The configuration pod is responsible for bootstrapping the initial environment.
 * It sets up component scanning 
 * <ul>
 * <li> Component Scanning </li>
 * <li> Configurations </li>
 * </ul>
 * By default it does not scan com.geekbeast.rhizome.pods. Each pod should be registered as required 
 * in the {@code RhizomeService.initialize(...)} method.
 * @author Matthew Tamayo-Rios
 */
@Configuration
public class ConfigurationPod {
    private static final RhizomeConfiguration rhizomeConfiguration = ConfigurationService.StaticLoader.loadConfiguration( RhizomeConfiguration.class ); 
    private static final JettyConfiguration jettyConfiguration = ConfigurationService.StaticLoader.loadConfiguration( JettyConfiguration.class );
    
    @Bean
    public RhizomeConfiguration rhizomeConfiguration() {
        return getRhizomeConfiguration();
    }
    
    @Bean 
    public JettyConfiguration jettyConfiguration() {
        return getJettyConfiguration();
    }
    
    public static RhizomeConfiguration getRhizomeConfiguration() {
        return rhizomeConfiguration;
    }
    
    public static JettyConfiguration getJettyConfiguration() {
        return jettyConfiguration;
    }
}
