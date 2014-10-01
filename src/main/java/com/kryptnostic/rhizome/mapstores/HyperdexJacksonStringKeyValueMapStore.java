package com.kryptnostic.rhizome.mapstores;

import org.hyperdex.client.Client;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;

public class HyperdexJacksonStringKeyValueMapStore<V> extends BaseHyperdexJacksonKeyValueMapStore<String, V> {
    static {
        HyperdexPreconfigurer.configure();
    }

    public HyperdexJacksonStringKeyValueMapStore( String space , Client client , HyperdexMapper<V> mapper ) {
        super( space , client , mapper );
    }
}
