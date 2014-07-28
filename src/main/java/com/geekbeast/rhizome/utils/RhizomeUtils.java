package com.geekbeast.rhizome.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RhizomeUtils {
    private static final Logger logger = LoggerFactory
                                               .getLogger( RhizomeUtils.class );

    public static String loadResourceToString(final String path) {
        final InputStream stream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream( path );
        String resource = null;
        try {
            resource = IOUtils.toString( stream );
        } catch (final IOException | NullPointerException e) {
            logger.error( "Failed to load resource from " + path , e );
            return null;
        } finally {
            IOUtils.closeQuietly( stream );
        }

        return resource;
    }

}
