package com.geekbeast.rhizome.configuration.hyperdex;

import com.kryptnostic.rhizome.mapstores.MappingException;

public interface MapStoreKeyMapper<K> {
    /**
     * @param key
     * @return Object or String that can be serialized from key
     * @throws MappingException
     */
    Object getKey( K key ) throws MappingException;

    K fromString( String str ) throws MappingException;
}
