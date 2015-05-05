package com.kryptnostic.rhizome.mappers.keys;

import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class SimpleKeyMapper<K extends String> implements KeyMapper<K> {
    @Override
    public String fromKey( K key ) {
        return key.toString();
    }

    @Override
    public K toKey( String value ) throws MappingException {
        return (K) value;
    }
}
