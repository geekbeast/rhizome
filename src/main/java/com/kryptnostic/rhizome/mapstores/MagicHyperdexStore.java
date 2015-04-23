package com.kryptnostic.rhizome.mapstores;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;
import com.kryptnostic.rhizome.mappers.keys.JacksonHyperdexKeyMapper;
import com.kryptnostic.rhizome.mappers.values.TypeReferenceMapper;

public class MagicHyperdexStore<K, V> extends BaseHyperdexJacksonKeyValueMapStore<K, V> {

    public MagicHyperdexStore( String space, HyperdexClientPool pool, TypeReference<V> reference ) {
        super( space, pool, new JacksonHyperdexKeyMapper<K>(), new TypeReferenceMapper<V>( reference ) );
    }

}
