package com.geekbeast.rhizome.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class RhizomeUtils {
  private static final Logger logger = LoggerFactory.getLogger(RhizomeUtils.class);
    
    private static HashFunction hf = Hashing.murmur3_128();
    
    public static String loadResourceToString(final String path){
        final InputStream stream =
            Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
        try{
            return IOUtils.toString(stream);
        } catch(final IOException e){
            logger.error("Failed to load resource from " + path , e);
            throw new IllegalStateException(e);
        } finally{
            IOUtils.closeQuietly(stream);
        }
    }
    
}
