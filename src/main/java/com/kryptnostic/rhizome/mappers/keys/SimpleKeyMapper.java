package com.kryptnostic.rhizome.mappers.keys;

import com.geekbeast.rhizome.configuration.hyperdex.MapStoreKeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SimpleKeyMapper<K extends String> implements MapStoreKeyMapper<K> {
    @Override
    public String getKey( K key ) {
        return key.toString();
    }

    @Override
    public K fromString( String str ) throws MappingException {
        return (K) str;
    }
}
