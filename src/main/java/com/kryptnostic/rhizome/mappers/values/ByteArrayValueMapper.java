package com.kryptnostic.rhizome.mappers.values;

import com.kryptnostic.rhizome.mappers.ValueMapper;

public class ByteArrayValueMapper implements ValueMapper<byte[]> {

    @Override
    public byte[] toBytes( byte[] value ) {
        return value;
    }

    @Override
    public byte[] fromBytes( byte[] data ) {
        return data;
    }

}
