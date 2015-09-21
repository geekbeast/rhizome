package com.kryptnostic.rhizome.mapstores.hyperdex;

import org.apache.commons.lang3.RandomStringUtils;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mappers.keys.StringKeyMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public abstract class HyperdexJacksonStringKeyValueMapStore<V> extends HyperdexBaseJacksonKeyValueMapStore<String, V> {
    static {
        HyperdexPreconfigurer.configure();
    }

    public HyperdexJacksonStringKeyValueMapStore(
            String mapName,
            String space,
            HyperdexClientPool pool,
            ValueMapper<V> mapper ) {
        super( mapName, space, pool, new StringKeyMapper(), mapper );
    }

    @Override
    public String generateTestKey() {
        return RandomStringUtils.randomAlphanumeric( 10 );
    }

}
