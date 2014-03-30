package com.geekbeast.rhizome.pods;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.core.JettyLoam;
import com.geekbeast.rhizome.core.Loam;

@Configuration
public class ServletContainerPod {
    @Inject
    private JettyConfiguration jettyConfiguration;
    
    @Bean
    public Loam jettyServer() { 
        return new JettyLoam( jettyConfiguration );
    }
    
    
}
