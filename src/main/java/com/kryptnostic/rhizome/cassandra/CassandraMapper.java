package com.kryptnostic.rhizome.cassandra;

public interface CassandraMapper<V> {
    V map( String data );
    String asString( V input );
}
