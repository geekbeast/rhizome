package com.geekbeast.rhizome.tests.pods;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.servlets.DispatcherServletConfiguration;
import com.google.common.collect.Lists;

@Configuration
public class DispatcherServletsPod {
    @Bean
    public DispatcherServletConfiguration restServlet() {
        return new DispatcherServletConfiguration( "app" , new String[] { "/api/*" } , 1 , Lists.<Class<?>>newArrayList( RestfulServletPod.class ) );
    }
}
