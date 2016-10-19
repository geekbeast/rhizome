package com.geekbeast.rhizome.tests.bootstrap;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI;
import com.geekbeast.rhizome.tests.pods.DispatcherServletsPod;
import com.google.common.base.Optional;
import com.google.common.net.HttpHeaders;
import com.kryptnostic.rhizome.converters.RhizomeConverter;
import com.kryptnostic.rhizome.core.Rhizome;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;

import digital.loom.rhizome.authentication.Auth0SecurityTestPod;
import digital.loom.rhizome.authentication.Auth0TestPod;
import digital.loom.rhizome.authentication.AuthenticationTest;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

public class RhizomeTests {
    private final static Logger      logger     = LoggerFactory.getLogger( RhizomeTests.class );
    private static RestAdapter       adapter;
    public static final byte[]       TEST_BYTES = RandomUtils.nextBytes( 4096 );

    private static Rhizome           rhizome    = null;
    private static TestConfiguration expected   = null;

    @BeforeClass
    public static void plant() throws Exception {
        final String jwtToken = AuthenticationTest.authenticate().getLeft().getIdToken();
        rhizome = new Rhizome(
                Auth0TestPod.class,
                Auth0SecurityTestPod.class,
                DispatcherServletsPod.class,
                RegistryBasedHazelcastInstanceConfigurationPod.class );
        rhizome.sprout();
        logger.info( "Successfully started Rhizome microservice." );
        adapter = new RestAdapter.Builder().setEndpoint( "http://localhost:8081/rhizome/api" )
                .setRequestInterceptor(
                        (RequestInterceptor) facade -> facade.addHeader( "Authorization", "Bearer "  + jwtToken ) )
                .setConverter( new RhizomeConverter() )
                .setErrorHandler( new DefaultErrorHandler() )
                .setLogLevel( LogLevel.FULL )
                .setLog( new RestAdapter.Log() {
                    @Override
                    public void log( String msg ) {
                        logger.debug( msg.replaceAll( "%", "[percent]" ) );
                    }
                } )
                .build();

        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );

        TestConfiguration configuration = api.getTestConfiguration();
        Assert.assertNull( configuration );
        expected = new TestConfiguration(
                RandomStringUtils.random( 10 ),
                Optional.<String> absent() );
        TestConfiguration actual = api.setTestConfiguration( expected );
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testReadWriteConfiguration() {
        // We're assuming that data is not persisted across runs here.
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        TestConfiguration actual = api.getTestConfiguration();
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testGzip() throws IOException {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );

        Response response = api.gzipTest();

        Assert.assertNotNull( response );
        Assert.assertEquals( MediaType.TEXT_PLAIN, response.getBody().mimeType() );
        try ( InputStream in = response.getBody().in() ) {
            byte[] b = IOUtils.toByteArray( in );
            Assert.assertArrayEquals( TEST_BYTES, b );
        }
        for ( Header header : response.getHeaders() ) {
            if ( header.getName() == HttpHeaders.CONTENT_TYPE ) {
                Assert.assertEquals( "gzip", header.getValue() );
                break;
            }
        }

    }

    @Test
    public void teapotTest() throws IOException {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        Response raw;

        try {
            raw = api.teapot();
        } catch ( RetrofitError e ) {
            raw = e.getResponse();
        }

        Assert.assertEquals( HttpStatus.I_AM_A_TEAPOT.value(), raw.getStatus() );
        Assert.assertEquals( IOUtils.toString( raw.getBody().in() ), "I AM A TEAPOT!" );
    }

    @Test
    public void testSimpleControllerGets() throws Exception {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        ObjectMapper mapper = new ObjectMapper();
        logger.info( "Context configuration: {}", mapper.writeValueAsString( api.getContextConfiguration() ) );
        logger.info( "Jetty configuration: {}", mapper.writeValueAsString( api.getJettyConfiguration() ) );
    }

    @Test(
        expected = AccessDeniedException.class )
    public void testGetAdminEndpoint() {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        TestConfiguration actual = api.getTestConfigurationSecuredAdmin();
    }

    @Test
    public void testGetUserEndpoint() {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        TestConfiguration actual = api.getTestConfigurationSecuredUser();

        Assert.assertEquals( expected, actual );
    }

    @AfterClass
    public static void plow() throws BeansException, Exception {
        logger.info( "Finished testing loading servlet pod." );
        rhizome.wilt();
        logger.info( "Successfully shutdown Jetty, exiting main thread" );
    }
}
