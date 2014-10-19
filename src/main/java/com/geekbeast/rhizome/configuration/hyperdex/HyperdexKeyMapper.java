package com.geekbeast.rhizome.configuration.hyperdex;


public interface HyperdexKeyMapper<K> {
    Object getKey( K key );
}
