package com.kryptnostic.rhizome.hyperdex.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class HyperdexMappers {
    private HyperdexMappers() {}

    public static <V> SimpleHyperdexMapper<V> newMapper( Class<V> clazz, ObjectMapper mapper ) {
        return new SimpleHyperdexMapper<V>( clazz, mapper );
    }

    public static <V> SimpleHyperdexMapper<V> newMapper( Class<V> clazz ) {
        return new SimpleHyperdexMapper<V>( clazz );
    }
}
