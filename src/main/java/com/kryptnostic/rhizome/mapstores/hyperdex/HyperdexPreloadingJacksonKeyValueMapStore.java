package com.kryptnostic.rhizome.mapstores.hyperdex;

import java.util.Map;
import java.util.Set;

import org.hyperdex.client.Client;
import org.hyperdex.client.HyperDexClientException;
import org.hyperdex.client.Iterator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public abstract class HyperdexPreloadingJacksonKeyValueMapStore<K, V> extends HyperdexBaseJacksonKeyValueMapStore<K, V> {
    public HyperdexPreloadingJacksonKeyValueMapStore(
            String mapName,
            String space,
            HyperdexClientPool pool,
            KeyMapper<K> keyMapper,
            ValueMapper<V> valueMapper ) {
        super( mapName, space, pool, keyMapper, valueMapper );
    }

    @Override
    public Set<K> loadAllKeys() {
        Client client = pool.acquire();
        Set<K> keys = Sets.newHashSet();
        try {
            Iterator i = client.search( space, ImmutableMap.of() );
            while ( i.hasNext() ) {
                @SuppressWarnings( "unchecked" )
                Map<String, Object> obj = (Map<String, Object>) i.next();
                String objectId = obj.get( "id" ).toString();
                keys.add( keyMapper.toKey( objectId ) );
            }
        } catch ( HyperDexClientException e ) {
            logger.error( "Failed to load all keys.", e );
        } finally {
            pool.release( client );
        }
        return keys;
    }
}
