package com.kryptnostic.rhizome.mappers.keys;

import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;

public class StringKeyMapper implements SelfRegisteringKeyMapper<String> {
    @Override
    public String fromKey( String key ) {
        return key;
    }

    @Override
    public String toKey( String value ) {
        return value;
    }

    @Override
    public Class<String> getClazz() {
        return String.class;
    }
}
