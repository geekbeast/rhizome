package com.kryptnostic.rhizome.hyperdex.mappers;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;

public class StringHyperdexKeyMapper implements HyperdexKeyMapper<String> {
    @Override
    public Object getKey( String key ) {
        return key;
    }
}
