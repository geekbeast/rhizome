package com.kryptnostic.rhizome.configuration.cassandra;

import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.Session;

/**
 * Keeps track of named Cassandra sessions when running multiple Cassandra clusters.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class Sessions extends HashMap<String, Session> {
    private static final long serialVersionUID = 8259848372338304844L;

    public Sessions() {
        super();
    }

    public Sessions( int initialSize ) {
        super( initialSize );
    }

    public Sessions( Map<String, Session> sessions ) {
        super( sessions );
    }

}
