package com.kryptnostic.rhizome.mapstores.hyperdex;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mappers.keys.StringKeyMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public class HyperdexJacksonStringKeyValueMapStore<V> extends HyperdexBaseJacksonKeyValueMapStore<String, V> {
    static {
        HyperdexPreconfigurer.configure();
    }

    public HyperdexJacksonStringKeyValueMapStore( String space, HyperdexClientPool pool, ValueMapper<V> mapper ) {
        super( space, pool, new StringKeyMapper(), mapper );
    }
}
