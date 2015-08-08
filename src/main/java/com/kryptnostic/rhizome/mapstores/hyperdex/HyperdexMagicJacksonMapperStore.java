package com.kryptnostic.rhizome.mapstores.hyperdex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kryptnostic.rhizome.mappers.keys.JacksonKeyMapper;
import com.kryptnostic.rhizome.mappers.values.TypeReferenceValueMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public class HyperdexMagicJacksonMapperStore<K, V> extends HyperdexBaseJacksonKeyValueMapStore<K, V> {

    public HyperdexMagicJacksonMapperStore(
            String mapName,
            String space,
            HyperdexClientPool pool,
            TypeReference<V> reference ) {
        super( mapName, space, pool, new JacksonKeyMapper<K>(), new TypeReferenceValueMapper<V>( reference ) );
    }

}
