package com.kryptnostic.rhizome.mappers.keys;

import com.kryptnostic.rhizome.mappers.KeyMapper;

public class CastingKeyMapper<K extends String> implements KeyMapper<K> {
    @Override
    public String fromKey( K key ) {
        return key.toString();
    }

    @Override
    public K toKey( String value ) {
        return (K) value;
    }
}
