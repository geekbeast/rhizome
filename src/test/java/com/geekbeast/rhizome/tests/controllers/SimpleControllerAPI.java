package com.geekbeast.rhizome.tests.controllers;

import retrofit.client.Response;
import retrofit.http.Body;

import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.ContextConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;

public interface SimpleControllerAPI {
    String CONTROLLER = "/simple";

    interface GET {
        String GZIP_TEST             = "/gzip";
        String CONTEXT_CONFIGURATION = "/context";
        String JETTY_CONFIGURATION   = "/jetty";
        String TEST_CONFIGURATION    = "/test";
        String TEAPOT                = "/teapot";
        String SECURED_ADMIN         = "/secured/admin";
        String SECURED_USER          = "/secured/user";
    }

    interface PUT {
        String TEST_CONFIGURATION = "/test";
    }

    @retrofit.http.GET( GET.GZIP_TEST )
    Response gzipTest();

    @retrofit.http.GET( GET.CONTEXT_CONFIGURATION )
    ContextConfiguration getContextConfiguration();

    @retrofit.http.GET( GET.JETTY_CONFIGURATION )
    JettyConfiguration getJettyConfiguration();

    @retrofit.http.GET( GET.TEST_CONFIGURATION )
    TestConfiguration getTestConfiguration();

    @retrofit.http.GET( GET.SECURED_ADMIN )
    TestConfiguration getTestConfigurationSecuredAdmin();

    @retrofit.http.GET( GET.SECURED_USER )
    TestConfiguration getTestConfigurationSecuredUser();

    @retrofit.http.PUT( PUT.TEST_CONFIGURATION )
    TestConfiguration setTestConfiguration( @Body TestConfiguration configuration );

    @retrofit.http.GET( GET.TEAPOT )
    Response teapot();

}