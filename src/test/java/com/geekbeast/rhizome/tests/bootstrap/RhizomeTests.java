package com.geekbeast.rhizome.tests.bootstrap;

import com.dataloom.retrofit.LoomByteConverterFactory;
import com.dataloom.retrofit.LoomCallAdapterFactory;
import com.dataloom.retrofit.LoomJacksonConverterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI;
import com.geekbeast.rhizome.tests.pods.DispatcherServletsPod;
import com.google.common.base.Optional;
import com.google.common.net.HttpHeaders;
import com.kryptnostic.rhizome.core.Cutting;
import com.kryptnostic.rhizome.core.Rhizome;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import digital.loom.rhizome.authentication.Auth0SecurityTestPod;
import digital.loom.rhizome.authentication.Auth0TestPod;
import digital.loom.rhizome.authentication.AuthenticationTest;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.http.HttpStatus;
import retrofit2.Retrofit;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

public class RhizomeTests {
    private final static Logger      logger     = LoggerFactory.getLogger( RhizomeTests.class );
    private static Retrofit          adapter;
    public static final byte[]       TEST_BYTES = RandomUtils.nextBytes( 1 << 12 );

    private static Rhizome           rhizome    = null;
    private static TestConfiguration expected   = null;

    @BeforeClass
    public static void plant() throws Exception {
        final String jwtToken = AuthenticationTest.authenticate().getCredentials().getIdToken();
        rhizome = new Rhizome(
                Auth0TestPod.class,
                Auth0SecurityTestPod.class,
                DispatcherServletsPod.class,
                RegistryBasedHazelcastInstanceConfigurationPod.class );
        rhizome.sprout();
        logger.info( "Successfully started Rhizome microservice." );
        /*
         * These interceptor are finicky and order dependent. Jetty doesn't do gzip unless content is long enough so
         * only verify gzip is enabled if contentLength > 2K. Exact value is tough since it is post compression.
         */
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor( chain -> {
                    Response response = chain.proceed( chain.request() );
                    int responseCode = response.code();
                    if ( responseCode >= 200 && responseCode < 300 && response.body().contentLength() > 2048 ) {
                        Assert.assertTrue( "Content encoding header must be present",
                                response.headers().names().contains( HttpHeaders.CONTENT_ENCODING ) );
                        Assert.assertEquals( "gzip", response.headers().get( HttpHeaders.CONTENT_ENCODING ) );
                    }
                    return response;
                } )
                .addInterceptor( chain -> {
                    Response response = chain.proceed( chain.request() );
                    if ( response.code() == HttpStatus.I_AM_A_TEAPOT.value() ) {
                        Assert.assertTrue( StringUtils.startsWith( response.body().contentType().toString(),
                                MediaType.TEXT_PLAIN ) );
                        return response.newBuilder().code( 200 ).build();
                    }
                    return response;
                } )
                .addInterceptor( chain -> chain
                        .proceed( chain.request().newBuilder().addHeader( "Authorization", "Bearer " + jwtToken )
                                .build() ) )
                .build();
        adapter = new Retrofit.Builder().baseUrl( "http://localhost:8081/rhizome/api/" ).client( httpClient )
                .addConverterFactory( new LoomByteConverterFactory() )
                .addConverterFactory( new LoomJacksonConverterFactory() )
                .addCallAdapterFactory( new LoomCallAdapterFactory() ).build();

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
    public void getCutting() {
        Assert.assertNotNull( "Cuttings of the rhizome must always be present.",
                rhizome.getContext().getBean( Cutting.class ) );
    }

    @Test
    public void testReadWriteConfiguration() {
        // We're assuming that data is not persisted across runs here.
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        TestConfiguration actual = api.getTestConfiguration();
        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testByteArray() throws IOException {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );

        byte[] response = api.gzipTest();

        Assert.assertNotNull( response );
        Assert.assertArrayEquals( TEST_BYTES, response );
    }

    @Test
    public void teapotTest() throws IOException {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        Assert.assertEquals( "I AM A TEAPOT!", api.teapot() );
    }

    @Test
    public void testSimpleControllerGets() throws Exception {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        ObjectMapper mapper = new ObjectMapper();
        logger.info( "Context configuration: {}", mapper.writeValueAsString( api.getContextConfiguration() ) );
        logger.info( "Jetty configuration: {}", mapper.writeValueAsString( api.getJettyConfiguration() ) );
    }

    @Test
    public void testGetAdminEndpoint() {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        TestConfiguration actual = api.getTestConfigurationSecuredAdmin();
    }

    @Test
    public void testGetFooEndpoint() {
        SimpleControllerAPI api = adapter.create( SimpleControllerAPI.class );
        TestConfiguration actual = api.getTestConfigurationSecuredFoo();
        Assert.assertNull( actual );
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
