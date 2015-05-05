package com.kryptnostic.rhizome.mappers.values;

import com.kryptnostic.rhizome.mappers.ValueMapper;

public class StringValueMapper implements ValueMapper<String> {

    @Override
    public byte[] toBytes( String value ) {
        return value.getBytes();
    }

    @Override
    public String fromBytes( byte[] data ) {
        return data == null ? null : new String( data );
    }
}
