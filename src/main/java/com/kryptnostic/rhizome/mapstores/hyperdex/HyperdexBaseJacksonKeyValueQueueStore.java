package com.kryptnostic.rhizome.mapstores.hyperdex;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jersey.repackaged.com.google.common.collect.Maps;

import org.hyperdex.client.ByteString;
import org.hyperdex.client.Client;
import org.hyperdex.client.Deferred;
import org.hyperdex.client.HyperDexClientException;
import org.hyperdex.client.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.QueueStoreConfig;
import com.kryptnostic.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringQueueStore;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public class HyperdexBaseJacksonKeyValueQueueStore<T> implements SelfRegisteringQueueStore<T> {
    protected final Logger             logger = LoggerFactory.getLogger( getClass() );

    static {
        HyperdexPreconfigurer.configure();
    }

    protected final ValueMapper<T>     mapper;
    protected final HyperdexClientPool pool;
    protected final String             space;
    protected final String             queueName;

    public HyperdexBaseJacksonKeyValueQueueStore(
            String queueName,
            String space,
            HyperdexClientPool pool,
            ValueMapper<T> mapper ) {
        this.mapper = mapper;
        this.pool = pool;
        this.space = space;
        this.queueName = queueName;
    }

    protected <T> T doSafeOperation( ClientOperation<T> clientOperation ) throws HyperDexClientException {
        HyperDexClientException exception = null;
        T val = null;

        try {
            Client client = pool.acquire();
            val = clientOperation.exec( client );
            pool.release( client );
        } catch ( HyperDexClientException e ) {
            exception = e;
            logger.error( e.getMessage() );
        }

        if ( exception != null ) {
            throw exception;
        }

        return val;
    }

    protected interface ClientOperation<T> {
        T exec( Client client ) throws HyperDexClientException;
    }

    @Override
    public T load( Long key ) {
        Map<String, Object> value = null;
        try {

            value = doSafeOperation( ( client ) -> {
                return client.get( space, key );
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
                doSafeOperation( ( client ) -> {
                    client.del( space, key );
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
    public Map<Long, T> loadAll( final Collection<Long> keys ) {
        logger.info( "Loading {} keys", keys.size() );

        final Map<Long, Deferred> deferredValues = Maps.newHashMapWithExpectedSize( keys.size() );
        Client client = pool.acquire();

        keys.forEach( ( Long key ) -> {
            try {
                Deferred value = client.async_get( space, key );
                if ( value != null ) {
                    deferredValues.put( key, value );
                }
            } catch ( HyperDexClientException e ) {
                logger.error( "Error trying to submit async read to hyperdex for key {} in space {}", key, space, e );
            }
        } );

        Map<Long, T> values = Maps.newHashMapWithExpectedSize( keys.size() );

        for ( Map.Entry<Long, Deferred> entry : deferredValues.entrySet() ) {
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
    public Set<Long> loadAllKeys() {
        Client client = pool.acquire();
        Set<Long> keys = Sets.newHashSet();
        try {
            Iterator i = client.search( space, ImmutableMap.of() );
            while ( i.hasNext() ) {
                @SuppressWarnings( "unchecked" )
                Map<String, Object> obj = (Map<String, Object>) i.next();
                Long key = ( (Double) obj.get( "id" ) ).longValue();
                keys.add( key );
            }
        } catch ( HyperDexClientException e ) {
            logger.error( "Failed to load all keys.", e );
        } finally {
            pool.release( client );
        }
        return keys;
    }

    @Override
    public void store( Long key, T value ) {
        try {
            Map<String, Object> val = Maps.newHashMap();
            val.put( ValueMapper.DATA_ATTRIBUTE, ByteString.wrap( mapper.toBytes( value ) ) );
            doSafeOperation( ( client ) -> client.put( space, key, val ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to map object for key {} in space {}", key, space, e );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }
    }

    @Override
    public void storeAll( Map<Long, T> map ) {
        Set<Long> keys = map.keySet();
        final Map<Long, Deferred> deferredValues = Maps.newHashMapWithExpectedSize( keys.size() );
        Client client = this.pool.acquire();

        final AtomicInteger i = new AtomicInteger();
        keys.forEach( ( Long key ) -> {
            try {
                T val = map.get( key );
                ByteString data = ByteString.wrap( mapper.toBytes( val ) );
                Map<String, Object> attrs = Maps.newHashMap();
                attrs.put( ValueMapper.DATA_ATTRIBUTE, data );
                Deferred value = client.async_put( space, key, attrs );
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
        for ( Map.Entry<Long, Deferred> entry : deferredValues.entrySet() ) {
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
    public void delete( Long key ) {
        try {
            doSafeOperation( ( client ) -> client.del( space, key ) );
        } catch ( HyperDexClientException e ) {
            logger.error( "Couldn't get client when getting key {} in space {}", key, space, e );
        }
    }

    @Override
    public void deleteAll( Collection<Long> keys ) {
        keys.forEach( key -> delete( key ) );
    }

    @Override
    public QueueConfig getQueueConfig() {
        return new QueueConfig().setBackupCount( 2 )
                .setQueueStoreConfig( new QueueStoreConfig().setStoreImplementation( this ) ).setName( queueName );
    }

}
