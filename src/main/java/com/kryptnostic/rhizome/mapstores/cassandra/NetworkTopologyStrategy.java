package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Map;
import java.util.Map.Entry;

public class NetworkTopologyStrategy implements ReplicationStrategy {
    private static String              BASE = "{'class' : 'NetworkToplogyStrategy'";
    private final Map<String, Integer> replicationFactors;

    public NetworkTopologyStrategy( Map<String, Integer> replicationFactors ) {
        this.replicationFactors = replicationFactors;
    }

    @Override
    public String getCqlString() {
        String cqlString = BASE;
        for ( Entry<String, Integer> replicationFactor : replicationFactors.entrySet() ) {
            cqlString += ", '" + replicationFactor.getKey() + " : " + replicationFactor.getValue();
        }
        cqlString += " }";
        return cqlString;
    }

}
