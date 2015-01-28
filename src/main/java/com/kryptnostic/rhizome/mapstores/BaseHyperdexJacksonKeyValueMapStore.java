package com.kryptnostic.rhizome.mapstores;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jersey.repackaged.com.google.common.collect.Maps;

import org.hyperdex.client.Client;
import org.hyperdex.client.HyperDexClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.hazelcast.core.MapStore;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMapper;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;

public class BaseHyperdexJacksonKeyValueMapStore<K, V> implements MapStore<K, V> {

    private static final int             MAX_TRIES = 200;
    protected final Logger               logger    = LoggerFactory.getLogger( getClass() );

    static {
        HyperdexPreconfigurer.configure();
    }

    protected final HyperdexMapper<V>    mapper;
    protected final HyperdexClientPool   pool;
    protected final String               space;
    protected final HyperdexKeyMapper<K> keyMapper;

    public BaseHyperdexJacksonKeyValueMapStore(
            String space,
            HyperdexClientPool pool,
            HyperdexKeyMapper<K> keyMapper,
            HyperdexMapper<V> mapper ) {
        this.mapper = mapper;
        this.pool = pool;
        this.space = space;
        this.keyMapper = keyMapper;
    }

    private <T> T doSafeOperation( ClientOperation<T> op ) {
        boolean shouldTry = true;
        int numTries = 0;
        T val = null;

        while ( shouldTry && ++numTries < MAX_TRIES ) {
            try {
                val = op.exec();
                shouldTry = false;
            } catch ( HyperDexClientException e ) {
                logger.error( e.getMessage() );
            }
        }
        return val;
    }

    private interface ClientOperation<T> {
        T exec() throws HyperDexClientException;
    }

    @Override
    public V load( K key ) {
        Client client = pool.acquire();
        Map<String, Object> value = null;
        try {
            Object keyObject = keyMapper.getKey( key );
            value = doSafeOperation( ( ) -> client.get( space, keyObject ) );
            if ( value == null ) {
                return null;
            }
            return mapper.fromHyperdexMap( value );
        } catch ( HyperdexMappingException e ) {
            logger.error( "Unable to unmap returned object for key {} in space {}", key, space, e );
        } finally {
            pool.release( client );
        }

        return null;
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> values = Maps.newHashMapWithExpectedSize( keys.size() );
        for ( K key : keys ) {
            V value = load( key );
            if ( value != null ) {
                values.put( key, value );
            }
        }
        // keys.forEach( ( key ) -> {
        // V value = load( key );
        // if ( value != null ) {
        // values.put( key, value );
        // }
        // } );
        return values;
    }

    @Override
    public Set<K> loadAllKeys() {
        // Overload this if you want to load some keys by default at startup.
        return null;
    }

    @Override
    public void store( K key, V value ) {
        Client client = pool.acquire();
        try {
            Object keyObject = keyMapper.getKey( key );
            Map<String, Object> val = mapper.toHyperdexMap( value );
            doSafeOperation( ( ) -> client.put( space, keyObject, val ) );
        } catch ( HyperdexMappingException e ) {
            logger.error( "Unable to map object for key {} in space {}", key, space, e );
        } finally {
            pool.release( client );
        }
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        map.forEach( ( k, v ) -> store( k, v ) );
    }

    @Override
    public void delete( K key ) {
        Client client = pool.acquire();
        try {
            Object keyObject = keyMapper.getKey( key );
            doSafeOperation( ( ) -> client.del( space, keyObject ) );
        } catch ( HyperdexMappingException e ) {
            logger.error( "Error deleting key {} from hyperdex.", key, e );
        } finally {
            pool.release( client );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        keys.forEach( key -> delete( key ) );
    }
}
