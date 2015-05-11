package com.kryptnostic.rhizome.mappers;

import com.kryptnostic.rhizome.mapstores.MappingException;

public interface ValueMapper<V> {
    public static final String DATA_ATTRIBUTE = "data";

    byte[] toBytes( V value ) throws MappingException;

    V fromBytes( byte[] data ) throws MappingException;
}
