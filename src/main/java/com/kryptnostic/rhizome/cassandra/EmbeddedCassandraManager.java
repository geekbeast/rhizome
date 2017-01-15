package com.kryptnostic.rhizome.cassandra;

import com.datastax.driver.core.Cluster;

public interface EmbeddedCassandraManager {
    public void start( String yamlFile );

    default Cluster cluster() {
        throw new IllegalStateException( "Incorrect configuration. This ECM is unable to retrieve cluster." );
    };
}
