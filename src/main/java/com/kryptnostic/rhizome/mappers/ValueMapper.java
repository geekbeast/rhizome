package com.kryptnostic.rhizome.mappers;

import com.kryptnostic.rhizome.mapstores.MappingException;

public interface ValueMapper<V> {
    public static final String DATA_ATTRIBUTE = "data";
    public static final int    DEFAULT_BUFFER_SIZE = 16;

    byte[] toBytes( V value, int bufferSize ) throws MappingException;

    default byte[] toBytes( V value ) throws MappingException {
        return toBytes( value, DEFAULT_BUFFER_SIZE );
    }

    V fromBytes( byte[] data ) throws MappingException;
}
