package com.kryptnostic.rhizome.mappers;

public interface KeyMapper<K> {
    public static final String ID_ATTRIBUTE = "id";
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * @param key
     * @return Object or String that can be serialized from key
     */
    String fromKey( K key );

    K toKey( String value );
}
