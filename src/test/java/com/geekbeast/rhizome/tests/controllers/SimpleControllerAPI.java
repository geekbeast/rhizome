
package com.geekbeast.rhizome.tests.controllers;

import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.ContextConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface SimpleControllerAPI {
    String CONTROLLER = "/";

    interface ENDPOINTS {
        String GZIP_TEST                  = "unsecured/gzip";
        String CONTEXT_CONFIGURATION      = "unsecured/context";
        String JETTY_CONFIGURATION        = "unsecured/jetty";
        String TEST_CONFIGURATION         = "unsecured/test";
        String TEAPOT                     = "unsecured/teapot";
        String SECURED_ADMIN              = "secured/admin";
        String SECURED_FOO                = "secured/foo";
        String SECURED_USER               = "secured/user";
        String SECURED_TEST_CONFIGURATION = "secured/test";
    }

    @GET( ENDPOINTS.GZIP_TEST )
    byte[] gzipTest();

    @GET( ENDPOINTS.CONTEXT_CONFIGURATION )
    ContextConfiguration getContextConfiguration();

    @GET( ENDPOINTS.JETTY_CONFIGURATION )
    JettyConfiguration getJettyConfiguration();

    @GET( ENDPOINTS.TEST_CONFIGURATION )
    TestConfiguration getTestConfiguration();

    @GET( ENDPOINTS.SECURED_ADMIN )
    TestConfiguration getTestConfigurationSecuredAdmin();
    
    @GET( ENDPOINTS.SECURED_FOO )
    TestConfiguration getTestConfigurationSecuredFoo();

    @GET( ENDPOINTS.SECURED_USER )
    TestConfiguration getTestConfigurationSecuredUser();

    @PUT( ENDPOINTS.SECURED_TEST_CONFIGURATION )
    TestConfiguration setTestConfiguration( @Body TestConfiguration configuration );

    @GET( ENDPOINTS.TEAPOT )
    String teapot();


}