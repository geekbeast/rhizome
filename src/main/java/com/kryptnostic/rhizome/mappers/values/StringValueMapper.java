package com.kryptnostic.rhizome.mappers.values;

import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;

public class StringValueMapper implements SelfRegisteringValueMapper<String> {

    @Override
    public byte[] toBytes( String value, int bufferSize ) {
        return value.getBytes();
    }

    @Override
    public String fromBytes( byte[] data ) {
        return data == null ? null : new String( data );
    }

    @Override
    public Class<String> getClazz() {
        return String.class;
    }
}
