package com.kryptnostic.rhizome.mapstores.hyperdex;

import com.kryptnostic.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public class HyperdexJacksonStringKeyValueQueueStore<T> extends HyperdexBaseJacksonKeyValueQueueStore<T> {
    static {
        HyperdexPreconfigurer.configure();
    }

    public HyperdexJacksonStringKeyValueQueueStore(
            String queueName,
            String space,
            HyperdexClientPool pool,
            ValueMapper<T> mapper ) {
        super( queueName, space, pool, mapper );
    }
}
