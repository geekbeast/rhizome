package com.kryptnostic.rhizome.mappers.keys;

import com.geekbeast.rhizome.configuration.hyperdex.MapStoreKeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class StringKeyMapper implements MapStoreKeyMapper<String> {
    @Override
    public String getKey( String key ) {
        return key;
    }

    @Override
    public String fromString( String str ) throws MappingException {
        return str;
    }
}
