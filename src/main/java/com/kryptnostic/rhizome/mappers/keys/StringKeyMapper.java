package com.kryptnostic.rhizome.mappers.keys;

import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class StringKeyMapper implements KeyMapper<String> {
    @Override
    public String fromKey( String key ) {
        return key;
    }

    @Override
    public String toKey( String value ) throws MappingException {
        return value;
    }
}
