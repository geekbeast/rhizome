package com.kryptnostic.rhizome.mapstores;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMapper;
import com.kryptnostic.rhizome.hyperdex.mappers.keys.HyperdexKeyMappers;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;

public class HyperdexJacksonStringKeyValueMapStore<V> extends BaseHyperdexJacksonKeyValueMapStore<String, V> {
    static {
        HyperdexPreconfigurer.configure();
    }

    public HyperdexJacksonStringKeyValueMapStore( String space, HyperdexClientPool pool, HyperdexMapper<V> mapper ) {
        super( space, pool, HyperdexKeyMappers.newStringPassthrough(), mapper );
    }
}
