package com.kryptnostic.rhizome.hyperdex.mappers.keys;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;

public class StringHyperdexKeyMapper implements HyperdexKeyMapper<String> {
    @Override
    public String getKey( String key ) {
        return key;
    }
}
