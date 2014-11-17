package com.kryptnostic.rhizome.mapstores;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kryptnostic.rhizome.hyperdex.mappers.keys.JacksonHyperdexKeyMapper;
import com.kryptnostic.rhizome.hyperdex.mappers.values.TypeReferenceHyperdexMapper;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;

public class MagicHyperdexStore<K, V> extends BaseHyperdexJacksonKeyValueMapStore<K, V> {

    public MagicHyperdexStore( String space, HyperdexClientPool pool, TypeReference<V> reference ) {
        super( space, pool, new JacksonHyperdexKeyMapper<K>(), new TypeReferenceHyperdexMapper<V>( reference ) );
    }

}
