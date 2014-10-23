package com.kryptnostic.rhizome.hyperdex.mappers;

import java.util.Map;

import com.kryptnostic.rhizome.mapstores.HyperdexMappingException;

public interface HyperdexMapper<V> {
    Map<String, Object> toHyperdexMap( V value ) throws HyperdexMappingException;

    V fromHyperdexMap( Map<String, Object> hyperdexMap ) throws HyperdexMappingException;
}
