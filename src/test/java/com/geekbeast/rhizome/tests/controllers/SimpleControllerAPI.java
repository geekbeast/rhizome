package com.geekbeast.rhizome.tests.controllers;

import retrofit.client.Response;
import retrofit.http.Body;

import com.geekbeast.rhizome.configuration.jetty.ContextConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;

public interface SimpleControllerAPI {
    String CONTROLLER = "/simple";
    interface GET {
        String CONTEXT_CONFIGURATION = "/context";
        String JETTY_CONFIGURATION = "/jetty";
        String TEST_CONFIGURATION = "/test";
        String TEAPOT = "/teapot";
    }
    
    interface PUT {
        String TEST_CONFIGURATION = "/test";
    }
    
    @retrofit.http.GET(GET.CONTEXT_CONFIGURATION)
    ContextConfiguration getContextConfiguration();
    
    @retrofit.http.GET(GET.JETTY_CONFIGURATION)
    JettyConfiguration getJettyConfiguration();

    @retrofit.http.GET(GET.TEST_CONFIGURATION)
    TestConfiguration getTestConfiguration();

    @retrofit.http.PUT(PUT.TEST_CONFIGURATION)
    TestConfiguration setTestConfiguration(@Body TestConfiguration configuration);

    @retrofit.http.GET(GET.TEAPOT)
    Response teapot();

}