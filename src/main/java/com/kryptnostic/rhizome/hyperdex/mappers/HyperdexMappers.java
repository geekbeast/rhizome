package com.kryptnostic.rhizome.hyperdex.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptnostic.rhizome.hyperdex.mappers.values.StringHyperdexMapper;

public final class HyperdexMappers {
    private static final StringHyperdexMapper stringHyperdexMapper = new StringHyperdexMapper();
    private HyperdexMappers() {}
    
    public static <V> SimpleHyperdexMapper<V> newMapper( Class<V> clazz, ObjectMapper mapper ) {
        return new SimpleHyperdexMapper<V>( clazz, mapper );
    }

    public static <V> SimpleHyperdexMapper<V> newMapper( Class<V> clazz ) {
        return new SimpleHyperdexMapper<V>( clazz );
    }
    
    public static StringHyperdexMapper newStringMapper() {
        return stringHyperdexMapper;
    }
}
