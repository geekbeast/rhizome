package com.kryptnostic.rhizome.mappers.values;

import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;

public class ByteArrayValueMapper implements SelfRegisteringValueMapper<byte[]> {

    @Override
    public byte[] toBytes( byte[] value, int bufferSize ) {
        return value;
    }

    @Override
    public byte[] fromBytes( byte[] data ) {
        return data;
    }

    @Override
    public Class<byte[]> getClazz() {
        return byte[].class;
    }

}
