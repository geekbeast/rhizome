package com.kryptnostic.rhizome.mappers.values;

import java.util.Map;

import org.hyperdex.client.ByteString;

import com.google.common.collect.ImmutableMap;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class StringMapper implements MapStoreDataMapper<String> {

    @Override
    public Map<String, Object> toValueMap( String value ) {
        return ImmutableMap.of( DATA_ATTRIBUTE, value );
    }

    @Override
    public String fromValueMap( Map<String, Object> hyperdexMap ) {
        ByteString value = ( (ByteString) hyperdexMap.get( DATA_ATTRIBUTE ) );
        return value == null ? null : value.toString();
    }

    @Override
    public String fromString( String str ) throws MappingException {
        return str;
    }

    @Override
    public String toValueString( String value ) throws MappingException {
        return value;
    }
}
