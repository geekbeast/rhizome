package com.kryptnostic.rhizome.mapstores;

import java.util.Map;

public interface HyperdexMapper<V> {
    Map<String, Object> toHyperdexMap( V value ) throws HyperdexMappingException;
    V fromHyperdexMap( Map<String, Object> hyperdexMap ) throws HyperdexMappingException ;
}
