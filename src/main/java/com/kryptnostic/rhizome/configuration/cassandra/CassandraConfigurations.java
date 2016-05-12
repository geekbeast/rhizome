package com.kryptnostic.rhizome.configuration.cassandra;

import java.util.HashMap;
import java.util.Map;

public class CassandraConfigurations extends HashMap<String, CassandraConfiguration> {
    private static final long serialVersionUID = -624423583792146800L;

    public CassandraConfigurations() {
        super();
    }

    public CassandraConfigurations( int initialSize ) {
        super( initialSize );
    }

    public CassandraConfigurations( Map<String, CassandraConfiguration> configurations ) {
        super( configurations );
    }

}
