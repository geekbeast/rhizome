package com.geekbeast.rhizome.tests.controllers;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.geekbeast.rhizome.tests.bootstrap.RhizomeTests;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;
import com.google.common.base.Preconditions;
import com.kryptnostic.rhizome.configuration.jetty.ContextConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

import retrofit.client.Response;

@Controller
public class SimpleController implements SimpleControllerAPI {
    private static Logger        logger = LoggerFactory.getLogger( SimpleController.class );
    @Inject
    private ConfigurationService configurationService;

    @Inject
    private JettyConfiguration   jettyConfiguration;

    /*
     * (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#getContextConfiguration()
     */
    @Override
    @RequestMapping(
        value = GET.CONTEXT_CONFIGURATION,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON )
    public @ResponseBody ContextConfiguration getContextConfiguration() {
        return jettyConfiguration.getContextConfiguration().get();
    }

    /*
     * (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#getJettyConfiguration()
     */
    @Override
    @RequestMapping(
        value = GET.JETTY_CONFIGURATION,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON )
    public @ResponseBody JettyConfiguration getJettyConfiguration() {
        return jettyConfiguration;
    }

    /*
     * (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#getTestConfiguration()
     */
    @Override
    @RequestMapping(
        value = GET.TEST_CONFIGURATION,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON )
    public @ResponseBody TestConfiguration getTestConfiguration() {
        try {
            return configurationService.getConfiguration( TestConfiguration.class );
        } catch ( Exception e ) {
            logger.error( "Failed to get test configuration.", e );
            return null;
        }
    }
    
    @Override
    @RequestMapping(
        value = GET.SECURED_ADMIN,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON )
    public @ResponseBody TestConfiguration getTestConfigurationSecuredAdmin() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        
        try {
            return configurationService.getConfiguration( TestConfiguration.class );
        } catch ( Exception e ) {
            logger.error( "Failed to get test configuration.", e );
            return null;
        }
    }
    
    @Override
    @RequestMapping(
        value = GET.SECURED_USER,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON )
    public @ResponseBody TestConfiguration getTestConfigurationSecuredUser() {
        try {
            return configurationService.getConfiguration( TestConfiguration.class );
        } catch ( Exception e ) {
            logger.error( "Failed to get test configuration.", e );
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#setTestConfiguration(com.geekbeast.rhizome.tests.
     * configurations.TestConfiguration)
     */
    @Override
    @RequestMapping(
        value = PUT.TEST_CONFIGURATION,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON,
        consumes = MediaType.APPLICATION_JSON )
    public @ResponseBody TestConfiguration setTestConfiguration( @RequestBody TestConfiguration configuration ) {
        if( configuration == null ) {
            return null;
        }
        
        try {
            configurationService.setConfiguration( configuration );
        } catch ( IOException e ) {
            return null;
        }
        
        try {
            return Preconditions.checkNotNull( configurationService.getConfiguration( TestConfiguration.class ) );
        } catch ( IOException e ) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#teapot()
     */
    @Override
    public Response teapot() {
        // Empty impl, not the cleanest, but its pretty rare to have httpstatus only api calls
        return null;
    }

    @RequestMapping(
        value = GET.TEAPOT,
        method = RequestMethod.GET )
    public ResponseEntity<String> teapot( HttpServletResponse response ) {
        teapot();
        return new ResponseEntity<>( "I AM A TEAPOT!", HttpStatus.I_AM_A_TEAPOT );
    }

    @Override
    public Response gzipTest() {
        return null;
    }

    @RequestMapping(
        value = GET.GZIP_TEST,
        method = RequestMethod.GET,
        produces = MediaType.TEXT_PLAIN )
    public @ResponseBody byte[] gzipTestHandler() {
        return RhizomeTests.TEST_BYTES;
    }
}
