package com.kryptnostic.rhizome.mappers;

import com.kryptnostic.rhizome.mapstores.MappingException;

public interface KeyMapper<K> {
    public static final String ID_ATTRIBUTE = "id";
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * @param key
     * @return Object or String that can be serialized from key
     * @throws MappingException
     */
    String fromKey( K key ) throws MappingException;

    K toKey( String value ) throws MappingException;
}
