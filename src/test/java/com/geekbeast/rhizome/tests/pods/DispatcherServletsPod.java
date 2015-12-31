package com.geekbeast.rhizome.tests.pods;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;
import com.kryptnostic.rhizome.configuration.servlets.DispatcherServletConfiguration;

@Configuration
public class DispatcherServletsPod {
    @Bean
    public DispatcherServletConfiguration restServlet() {
        return new DispatcherServletConfiguration(
                "app",
                new String[] { "/api/*" },
                1,
                Lists.<Class<?>> newArrayList( RestfulServletPod.class ) );
    }
}
