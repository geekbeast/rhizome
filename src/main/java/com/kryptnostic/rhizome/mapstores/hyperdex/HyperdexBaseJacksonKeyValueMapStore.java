package com.kryptnostic.rhizome.mapstores.hyperdex;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.hyperdex.client.ByteString;
import org.hyperdex.client.Client;
import org.hyperdex.client.Deferred;
import org.hyperdex.client.HyperDexClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

import jersey.repackaged.com.google.common.collect.Maps;

@Deprecated
public abstract class HyperdexBaseJacksonKeyValueMapStore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    protected final Logger             logger = LoggerFactory.getLogger( getClass() );

    static {
        HyperdexPreconfigurer.configure();
    }

    protected final String             mapName;
    protected final ValueMapper<V>     mapper;
    protected final HyperdexClientPool pool;
    protected final String             space;
    protected final KeyMapper<K>       keyMapper;

    public HyperdexBaseJacksonKeyValueMapStore(
            String mapName,
            String space,
            HyperdexClientPool pool,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        this.mapName = mapName;
        this.mapper = mapper;
        this.pool = pool;
        this.space = space;
        this.keyMapper = keyMapper;
        RegistryBasedHazelcastInstanceConfigurationPod.register( mapName, this );
    }

    protected <T> T doSafeOperation( ClientOperation<T> clientOperation ) throws HyperDexClientException {
        // int numTries = 0;
        HyperDexClientException exception = null;
        T val = null;

        // while ( ++numTries < MAX_TRIES ) {
        // exception = null;
        try {
            Client client = pool.acquire();
            val = clientOperation.exec( client );
            pool.release( client );
            // break;
        } catch ( HyperDexClientException e ) {
            exception = e;
            logger.error( e.getMessage() );
        }
        // }

        if ( exception != null ) {
            throw exception;
        }

        return val;
    }

    protected interface ClientOperation<T> {
        T exec( Client client ) throws HyperDexClientException;
    }

    @Override
    public V load( K key ) {
        Map<String, Object> value = null;
        try {
            Object keyObject = keyMapper.fromKey( key );

            value = doSafeOperation( ( client ) -> {
                return client.get( space, keyObject );
            } );

            if ( value == null ) {
                return null;
            }
            ByteString rawData = (ByteString) value.get( ValueMapper.DATA_ATTRIBUTE );
            return mapper.fromBytes( rawData.getBytes() );
        } catch ( MappingException e ) {
            logger.error( "Unable to unmap returned object for key {} in space {}", key, space, e );
            try {
                logger.info( "Removing corrupted key {} in space {}", key, space );
                Object keyObject = keyMapper.fromKey( key );
                doSafeOperation( ( client ) -> {
                    client.del( space, keyObject );
                    return null;
                } );
            } catch ( HyperDexClientException e1 ) {
                logger.error( "Couldn't get client when getting key {} in space {}", key, space, e1 );
            }
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        } catch ( Exception e ) {
            logger.error( "Unexpected exception when getting key {} in space {}", key, space, e );
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Map<K, V> loadAll( final Collection<K> keys ) {
        logger.info( "Loading {} keys", keys.size() );
        // Map<K,V> values = Maps.newHashMapWithExpectedSize( keys.size() );
        // for( K key : keys ) {
        // V value = load( key );
        // if( value != null ) {
        // values.put( key, value );
        // }
        // }
        // return values;
        //
        final Map<K, Deferred> deferredValues = Maps.newHashMapWithExpectedSize( keys.size() );
        Client client = pool.acquire();

        keys.forEach( ( K key ) -> {
            Object keyObject;
            try {
                keyObject = keyMapper.fromKey( key );
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
                Map<String, Object> object = (Map<String, Object>) entry.getValue().waitForIt();
                ByteString rawData = (ByteString) object.get( ValueMapper.DATA_ATTRIBUTE );
                values.put( entry.getKey(), mapper.fromBytes( rawData.getBytes() ) );
            } catch ( MappingException | HyperDexClientException e ) {
                if ( e instanceof HyperdexMappingException ) {
                    delete( entry.getKey() );
                }
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
            Object keyObject = keyMapper.fromKey( key );
            Map<String, Object> val = Maps.newHashMap();
            val.put( ValueMapper.DATA_ATTRIBUTE, ByteString.wrap( mapper.toBytes( value ) ) );
            doSafeOperation( ( client ) -> client.put( space, keyObject, val ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to map object for key {} in space {}", key, space, e );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        Set<K> keys = map.keySet();
        final Map<K, Deferred> deferredValues = Maps.newHashMapWithExpectedSize( keys.size() );
        Client client = this.pool.acquire();

        final AtomicInteger i = new AtomicInteger();
        keys.forEach( ( K key ) -> {
            Object keyObject;
            try {
                keyObject = keyMapper.fromKey( key );
            } catch ( Exception e ) {
                logger.error( "Unable to read key for object key {} in space", key, space, e );
                return;
            }
            try {
                V val = map.get( key );
                ByteString data = ByteString.wrap( mapper.toBytes( val ) );
                Map<String, Object> attrs = Maps.newHashMap();
                attrs.put( ValueMapper.DATA_ATTRIBUTE, data );
                Deferred value = client.async_put( space, keyObject, attrs );
                if ( value != null ) {
                    deferredValues.put( key, value );
                }
            } catch ( MappingException e ) {
                logger.error( "Unable to map object for key {} in space {}", key, space, e );
            } catch ( HyperDexClientException e ) {
                logger.error( "Error trying to submit async put to hyperdex for key {} in space {}", key, space, e );
            }
            if ( i.incrementAndGet() % 1000 == 0 ) {
                logger.info( "Put {} so far", i );
            }

        } );

        int loaded = 0;
        logger.info( "Now waiting for deferred entries" );
        for ( Map.Entry<K, Deferred> entry : deferredValues.entrySet() ) {
            try {
                entry.getValue().waitForIt();
                loaded++;
            } catch ( HyperDexClientException e ) {
                logger.error( "Unable to put object for key {} in space {}", entry.getKey(), space, e );
            }

        }
        pool.release( client );
        logger.info( "Put {}/{} keys.", loaded, keys.size() );
    }

    @Override
    public void delete( K key ) {
        try {
            Object keyObject = keyMapper.fromKey( key );
            doSafeOperation( ( client ) -> client.del( space, keyObject ) );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        keys.forEach( key -> delete( key ) );
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return new MapStoreConfig().setImplementation( this ).setEnabled( true );
    }

    @Override
    public MapConfig getMapConfig() {
        return new MapConfig().setBackupCount( 2 ).setMapStoreConfig( getMapStoreConfig() ).setName( mapName );
    }

    @Override
    public String getMapName() {
        return this.mapName;
    }

    @Override
    public String getTable() {
        return this.space;
    }
}
