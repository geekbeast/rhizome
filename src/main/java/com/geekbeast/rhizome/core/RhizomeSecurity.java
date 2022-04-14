package com.geekbeast.rhizome.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * This class registers the Spring Security Filter chain with all servlet contexts. Do not remove.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class RhizomeSecurity extends AbstractSecurityWebApplicationInitializer {
    private static final Logger logger = LoggerFactory.getLogger( RhizomeSecurity.class );
    /* No-Op */

    protected RhizomeSecurity() {
        super();
        logger.info("Spring security initializer was propertly detected.");
    }
}
