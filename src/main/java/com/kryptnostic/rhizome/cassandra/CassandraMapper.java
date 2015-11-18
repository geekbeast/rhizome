package com.kryptnostic.rhizome.cassandra;

import com.kryptnostic.rhizome.mappers.ValueMapper;

public interface CassandraMapper<V> extends ValueMapper<V> {
    V map( String data );
    String asString( V input );
}
