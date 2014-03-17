package com.geekbeast.rhizome.pods;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.containers.JettyConfiguration;
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
@ComponentScan(
        basePackages={
                "com.geekbeast.rhizome.configuration.service" , 
                "com.geekbeast.rhizome.configuration.core" ,
                "com.geekbeast.rhizome.controllers"
                }, 
        excludeFilters = @ComponentScan.Filter( 
                value = {
                    org.springframework.stereotype.Controller.class , 
                } ,
       
                type = FilterType.ANNOTATION 
                )
        )
public class ConfigurationPod {
    
    @Bean
    public RhizomeConfiguration rhizomeConfiguration() {
        return ConfigurationService.StaticLoader.loadConfiguration( RhizomeConfiguration.class );
    }
    
    @Bean 
    public JettyConfiguration jettyConfiguration() {
        return ConfigurationService.StaticLoader.loadConfiguration( JettyConfiguration.class );
    }
}
