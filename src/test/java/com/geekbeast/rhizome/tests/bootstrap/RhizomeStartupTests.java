package com.geekbeast.rhizome.tests.bootstrap;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.service.ConfigurationService;
import com.geekbeast.rhizome.core.Rhizome;

public class RhizomeStartupTests extends Rhizome {
    private final static Logger logger = LoggerFactory.getLogger( RhizomeStartupTests.class );
    
    @Test
    public void testUpDown() throws Exception {
        Rhizome rhizome = new Rhizome();
        rhizome.sprout();
        logger.info("Successfully started Jetty, exiting main thread");
        rhizome.wilt();
        logger.info("Successfully shutdown Jetty, exiting main thread");
    }
    
    @Test
    public void testHarvestFromRootContext() throws Exception {
        Rhizome rhizome = new Rhizome();
        rhizome.sprout();
        ConfigurationService controller = rhizome.harvest( ConfigurationService.class );
        Assert.assertNotNull( controller );
    }
}
