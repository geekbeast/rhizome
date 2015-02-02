package com.kryptnostic.rhizome.mapstores;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import jersey.repackaged.com.google.common.collect.Maps;

import org.hyperdex.client.Client;
import org.hyperdex.client.Deferred;
import org.hyperdex.client.HyperDexClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hazelcast.core.MapStore;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMapper;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;

public class BaseHyperdexJacksonKeyValueMapStore<K, V> implements MapStore<K, V> {
    private static final ListeningExecutorService executor  = MoreExecutors.listeningDecorator( Executors
                                                                    .newCachedThreadPool() );
    private static final int                      MAX_TRIES = 200;
    protected final Logger                        logger    = LoggerFactory.getLogger( getClass() );

    static {
        HyperdexPreconfigurer.configure();
    }

    protected final HyperdexMapper<V>             mapper;
    protected final HyperdexClientPool            pool;
    protected final String                        space;
    protected final HyperdexKeyMapper<K>          keyMapper;

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

    private <T> T doSafeOperation( ClientOperation<T> clientOperation ) throws HyperDexClientException {
        int numTries = 0;
        HyperDexClientException exception = null;
        T val = null;

        while ( ++numTries < MAX_TRIES ) {
            exception = null;
            try {
                Client client = pool.acquire();
                val = clientOperation.exec( client );
                pool.release( client );
                break;
            } catch ( HyperDexClientException e ) {
                exception = e;
                logger.error( e.getMessage() );
            }
        }

        if ( exception != null ) {
            throw exception;
        }

        return val;
    }

    private interface ClientOperation<T> {
        T exec( Client client ) throws HyperDexClientException;
    }

    @Override
    public V load( K key ) {
        Map<String, Object> value = null;
        try {
            Object keyObject = keyMapper.getKey( key );
            value = doSafeOperation( ( client ) -> {
                return client.get( space, keyObject );
            } );

            if ( value == null ) {
                return null;
            }
            return mapper.fromHyperdexMap( value );
        } catch ( HyperdexMappingException e ) {
            logger.error( "Unable to unmap returned object for key {} in space {}", key, space, e );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Map<K, V> loadAll( final Collection<K> keys ) {
        logger.info( "Loading {} keys", keys.size() );
//        Map<K,V> values = Maps.newHashMapWithExpectedSize( keys.size() );
//        for( K key : keys ) {
//            V value = load( key );
//            if( value != null ) { 
//                values.put( key, value );
//            }
//        }
//        return values;
//        
        final Map<K, Deferred> deferredValues = Maps.newHashMapWithExpectedSize( keys.size() );
        Client client = pool.acquire();

        keys.forEach( ( K key ) -> {
            Object keyObject;
            try {
                keyObject = keyMapper.getKey( key );
            } catch ( Exception e ) {
                logger.error( "Unable to read key for object key {} in space", key, space, e );
                return;
            }
            try {
                Deferred value = client.async_get( space, keyObject );
                if ( value != null ) {
                    deferredValues.put( key, value );
                }
            } catch ( HyperDexClientException e ) {
                logger.error( "Error trying to submit async read to hyperdex for key {} in space {}", key, space, e );
            }
        } );

        Map<K, V> values = Maps.newHashMapWithExpectedSize( keys.size() );

        for ( Map.Entry<K, Deferred> entry : deferredValues.entrySet() ) {
            try {
                values.put( entry.getKey(), mapper.fromHyperdexMap( (Map<String, Object>) entry.getValue().waitForIt() ) );
            } catch ( HyperdexMappingException | HyperDexClientException e ) {
                logger.error( "Unable to unmap returned object for key {} in space {}", entry.getKey(), space, e );
            }

        }
        pool.release( client );
        logger.info( "Loaded {} keys.", values.size() );
        return values;
    }

    @Override
    public Set<K> loadAllKeys() {
        // Overload this if you want to load some keys by default at startup.
        return null;
    }

    @Override
    public void store( K key, V value ) {
        try {
            Object keyObject = keyMapper.getKey( key );
            Map<String, Object> val = mapper.toHyperdexMap( value );
            doSafeOperation( ( client ) -> client.put( space, keyObject, val ) );
        } catch ( HyperdexMappingException e ) {
            logger.error( "Unable to map object for key {} in space {}", key, space, e );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        map.forEach( ( k, v ) -> store( k, v ) );
    }

    @Override
    public void delete( K key ) {
        try {
            Object keyObject = keyMapper.getKey( key );
            doSafeOperation( ( client ) -> client.del( space, keyObject ) );
        } catch ( HyperdexMappingException e ) {
            logger.error( "Error deleting key {} from hyperdex.", key, e );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        keys.forEach( key -> delete( key ) );
    }
}
