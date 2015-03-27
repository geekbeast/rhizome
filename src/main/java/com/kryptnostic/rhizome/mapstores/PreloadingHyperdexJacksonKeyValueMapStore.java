package com.kryptnostic.rhizome.mapstores;

import java.util.Map;
import java.util.Set;

import org.hyperdex.client.Client;
import org.hyperdex.client.HyperDexClientException;
import org.hyperdex.client.Iterator;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMapper;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;

public class PreloadingHyperdexJacksonKeyValueMapStore<V> extends BaseHyperdexJacksonKeyValueMapStore<String, V> {
    public PreloadingHyperdexJacksonKeyValueMapStore(
            String space,
            HyperdexClientPool pool,
            HyperdexKeyMapper<String> keyMapper,
            HyperdexMapper<V> valueMapper ) {
        super( space, pool, keyMapper, valueMapper );
    }

    @Override
    public Set<String> loadAllKeys() {
        Client client = pool.acquire();
        Set<String> keys = Sets.newHashSet();
        try {
            Iterator i = client.search( space, ImmutableMap.of() );
            try {
                while ( i.hasNext() ) {
                    @SuppressWarnings( "unchecked" )
                    Map<String, Object> obj = (Map<String, Object>) i.next();
                    keys.add( obj.get( "id" ).toString() );
                }
            } catch ( HyperDexClientException e ) {
                logger.error( "Failed to load all keys.", e );
            }
        } finally {
            pool.release( client );
        }
        return keys;
    }
}
