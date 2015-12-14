package com.geekbeast.rhizome.pods;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.core.JettyLoam;
import com.geekbeast.rhizome.core.Loam;

@Configuration
public class JettyContainerPod implements LoamPod {
    @Inject
    private JettyConfiguration jettyConfiguration;

    @Bean
    public Loam getLoam() throws IOException {
        return new JettyLoam( jettyConfiguration );
    }
}
