package com.kryptnostic.rhizome.mappers.keys;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;

public class ByteArrayKeyMapper implements SelfRegisteringKeyMapper<byte[]> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public String fromKey( byte[] key ) {
        return key == null ? null : new String( key, CHARSET );
    }

    @Override
    public byte[] toKey( String value ) {
        return value.getBytes( CHARSET );
    }

    @Override
    public Class<byte[]> getClazz() {
        return byte[].class;
    }

}
