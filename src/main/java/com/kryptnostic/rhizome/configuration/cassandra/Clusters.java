package com.kryptnostic.rhizome.configuration.cassandra;

import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.Cluster;

public class Clusters extends HashMap<String, Cluster> {
    private static final long serialVersionUID = -6991286308331152286L;

    public Clusters() {
        super();
    }

    public Clusters( Map<String, Cluster> clusters ) {
        super( clusters );
    }

    public Clusters( int initialSize ) {
        super( initialSize );
    }

}
