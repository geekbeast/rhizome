package com.kryptnostic.rhizome.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptnostic.rhizome.mappers.values.SimpleValueMapper;
import com.kryptnostic.rhizome.mappers.values.StringValueMapper;

public final class Mappers {
    private static final StringValueMapper stringHyperdexMapper = new StringValueMapper();
    private Mappers() {}
    
    public static <V> SimpleValueMapper<V> newMapper( Class<V> clazz, ObjectMapper mapper ) {
        return new SimpleValueMapper<V>( clazz, mapper );
    }

    public static <V> SimpleValueMapper<V> newMapper( Class<V> clazz ) {
        return new SimpleValueMapper<V>( clazz );
    }
    
    public static StringValueMapper newStringMapper() {
        return stringHyperdexMapper;
    }
}
