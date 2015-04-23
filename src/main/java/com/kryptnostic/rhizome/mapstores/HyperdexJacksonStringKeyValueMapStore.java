package com.kryptnostic.rhizome.mapstores;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.mappers.keys.KeyMappers;

public class HyperdexJacksonStringKeyValueMapStore<V> extends BaseHyperdexJacksonKeyValueMapStore<String, V> {
    static {
        HyperdexPreconfigurer.configure();
    }

    public HyperdexJacksonStringKeyValueMapStore( String space, HyperdexClientPool pool, MapStoreDataMapper<V> mapper ) {
        super( space, pool, KeyMappers.newStringPassthrough(), mapper );
    }
}
