package com.geekbeast.rhizome.configuration.hyperdex;

import com.kryptnostic.rhizome.mapstores.HyperdexMappingException;

public interface HyperdexKeyMapper<K> {
    Object getKey( K key ) throws HyperdexMappingException;
}
