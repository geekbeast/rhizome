package com.kryptnostic.rhizome.mapstores.cassandra;

public class SimpleStrategy implements ReplicationStrategy {
    private static String BASE = "{'class' : 'SimpleStrategy'";
    private final int     replicatonFactor;

    public SimpleStrategy( int replicationFactor ) {
        this.replicatonFactor = replicationFactor;
    }

    @Override
    public String getCqlString() {
        return BASE + ", 'replication_factor' : " + replicatonFactor + " }";
    }

}
