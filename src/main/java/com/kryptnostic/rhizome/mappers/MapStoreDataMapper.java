package com.kryptnostic.rhizome.mappers;

import java.util.Map;

import com.kryptnostic.rhizome.mapstores.MappingException;

public interface MapStoreDataMapper<V> {
    public static final String DATA_ATTRIBUTE = "data";

    Map<String, Object> toValueMap( V value ) throws MappingException;

    String toValueString( V value ) throws MappingException;

    V fromValueMap( Map<String, Object> valueMap ) throws MappingException;

    V fromString( String str ) throws MappingException;
}
