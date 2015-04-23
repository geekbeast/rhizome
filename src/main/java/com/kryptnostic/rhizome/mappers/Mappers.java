package com.kryptnostic.rhizome.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptnostic.rhizome.mappers.values.SimpleMapper;
import com.kryptnostic.rhizome.mappers.values.StringMapper;

public final class Mappers {
    private static final StringMapper stringHyperdexMapper = new StringMapper();
    private Mappers() {}
    
    public static <V> SimpleMapper<V> newMapper( Class<V> clazz, ObjectMapper mapper ) {
        return new SimpleMapper<V>( clazz, mapper );
    }

    public static <V> SimpleMapper<V> newMapper( Class<V> clazz ) {
        return new SimpleMapper<V>( clazz );
    }
    
    public static StringMapper newStringMapper() {
        return stringHyperdexMapper;
    }
}
