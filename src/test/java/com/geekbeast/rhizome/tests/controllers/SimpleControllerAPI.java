
package com.geekbeast.rhizome.tests.controllers;

import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.ContextConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface SimpleControllerAPI {
    String CONTROLLER = "/";

    interface ENDPOINTS {
        String GZIP_TEST                  = "rhizome/unsecured/gzip";
        String CONTEXT_CONFIGURATION      = "rhizome/unsecured/context";
        String JETTY_CONFIGURATION        = "rhizome/unsecured/jetty";
        String TEST_CONFIGURATION         = "rhizome/unsecured/test";
        String TEAPOT                     = "rhizome/unsecured/teapot";
        String SECURED_ADMIN              = "rhizome/secured/admin";
        String SECURED_USER               = "rhizome/secured/user";
        String SECURED_TEST_CONFIGURATION = "rhizome/test";
    }

    @GET( ENDPOINTS.GZIP_TEST )
    Response gzipTest();

    @GET( ENDPOINTS.CONTEXT_CONFIGURATION )
    ContextConfiguration getContextConfiguration();

    @GET( ENDPOINTS.JETTY_CONFIGURATION )
    JettyConfiguration getJettyConfiguration();

    @GET( ENDPOINTS.TEST_CONFIGURATION )
    TestConfiguration getTestConfiguration();

    @GET( ENDPOINTS.SECURED_ADMIN )
    TestConfiguration getTestConfigurationSecuredAdmin();

    @GET( ENDPOINTS.SECURED_USER )
    TestConfiguration getTestConfigurationSecuredUser();

    @PUT( ENDPOINTS.SECURED_TEST_CONFIGURATION )
    TestConfiguration setTestConfiguration( @Body TestConfiguration configuration );

    @GET( ENDPOINTS.TEAPOT )
    String teapot();

}