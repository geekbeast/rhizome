package com.kryptnostic.rhizome.mappers.keys;

import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;

public class ByteArrayKeyMapper implements SelfRegisteringKeyMapper<byte[]> {

    @Override
    public String fromKey( byte[] key ) {
        return key == null ? null : new String( key );
    }

    @Override
    public byte[] toKey( String value ) {
        return value.getBytes();
    }

    @Override
    public Class<byte[]> getClazz() {
        return byte[].class;
    }

}
