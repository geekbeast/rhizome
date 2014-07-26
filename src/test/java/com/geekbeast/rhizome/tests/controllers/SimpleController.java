package com.geekbeast.rhizome.tests.controllers;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import retrofit.client.Response;

import com.geekbeast.rhizome.configuration.jetty.ContextConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.configuration.service.ConfigurationService;
import com.geekbeast.rhizome.tests.configurations.TestConfiguration;

@Controller(SimpleControllerAPI.CONTROLLER)
public class SimpleController implements SimpleControllerAPI {
    private static Logger logger = LoggerFactory.getLogger( SimpleController.class );
    @Inject
    private ConfigurationService configurationService;
    
    @Inject 
    private JettyConfiguration jettyConfiguration;
    
    /* (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#getContextConfiguration()
     */
    @Override
    @RequestMapping(value=GET.CONTEXT_CONFIGURATION , method=RequestMethod.GET , produces=MediaType.APPLICATION_JSON)
    public @ResponseBody ContextConfiguration getContextConfiguration() {
        return jettyConfiguration.getContextConfiguration().get();
    }
    
    /* (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#getJettyConfiguration()
     */
    @Override
    @RequestMapping(value=GET.JETTY_CONFIGURATION , method=RequestMethod.GET , produces=MediaType.APPLICATION_JSON)
    public @ResponseBody JettyConfiguration getJettyConfiguration() {
        return jettyConfiguration;
    }
    
    /* (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#getTestConfiguration()
     */
    @Override
    @RequestMapping(value=GET.TEST_CONFIGURATION , method=RequestMethod.GET , produces=MediaType.APPLICATION_JSON)
    public @ResponseBody TestConfiguration getTestConfiguration() {
        try {
            return configurationService.getConfiguration( TestConfiguration.class );
        } catch (IOException e) {
            logger.error("Failed to rest test configuration." , e );
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#setTestConfiguration(com.geekbeast.rhizome.tests.configurations.TestConfiguration)
     */
    @Override
    @RequestMapping(value=PUT.TEST_CONFIGURATION , method=RequestMethod.PUT , produces=MediaType.APPLICATION_JSON,consumes=MediaType.APPLICATION_JSON)
    public @ResponseBody TestConfiguration setTestConfiguration( @RequestBody TestConfiguration configuration ) {
        try {
            configurationService.setConfiguration( configuration );
        } catch (IOException e) {
            return null;
        }
        try {
            return configurationService.getConfiguration( TestConfiguration.class );
        } catch (IOException e) {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see com.geekbeast.rhizome.tests.controllers.SimpleControllerAPI#teapot()
     */
    @Override
    public Response teapot() {
        //Empty impl, not the cleanest, but its pretty rare to have httpstatus only api calls
        return null;
    }
    
    @RequestMapping(value=GET.TEAPOT , method=RequestMethod.GET)
    public ResponseEntity<String> teapot(HttpServletResponse response){
       teapot();
       return new ResponseEntity<String>( "I AM A TEAPOT!" , HttpStatus.I_AM_A_TEAPOT );
    }
}
