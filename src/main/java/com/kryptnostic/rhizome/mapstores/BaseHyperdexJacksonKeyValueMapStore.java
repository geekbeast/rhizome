package com.kryptnostic.rhizome.mapstores;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jersey.repackaged.com.google.common.collect.Maps;

import org.hyperdex.client.Client;
import org.hyperdex.client.HyperDexClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.hazelcast.core.MapStore;

public class BaseHyperdexJacksonKeyValueMapStore<K,V> implements MapStore<K,V> {
    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    static {
        HyperdexPreconfigurer.configure();
    }
    
    protected final HyperdexMapper<V> mapper;
    protected final Client client;
    protected final String space;

    public BaseHyperdexJacksonKeyValueMapStore( String space , Client client , HyperdexMapper<V> mapper ) {
        this.mapper = mapper;
        this.client = client;
        this.space = space;
    }
    
    @Override
    public V load(K key) {
        try {
            return mapper.fromHyperdexMap( (Map<String,Object>) client.get( space, key ) );
        } catch (HyperdexMappingException e) {
            logger.error( "Unable to unmap returned object for key {} in space {}" , key , space , e );
        } catch (HyperDexClientException e) {
            logger.error( "Unable to load object from Hyperdex for key {} in space {}" , key , space , e );
        }
        return null;
    }

    @Override
    public Map<K, V> loadAll(Collection<K> keys) {
        Map<K,V> values = Maps.newHashMapWithExpectedSize( keys.size() );
        keys.forEach( (key) -> { 
            V value = load( key );
            if( value != null ) { 
                values.put( key, value );
            }
        });
        return values;
    }

    @Override
    public Set<K> loadAllKeys() {
        //Overload this if you want to load some keys by default at startup.
        return null;
    }

    @Override
    public void store(K key, V value) {
        try {
            client.put( space , key , mapper.toHyperdexMap( value ) );
        } catch (HyperdexMappingException e) {
            logger.error( "Unable to map object for key {} in space {}" , key , space , e );
        } catch (HyperDexClientException e) {
            logger.error( "Error storing object to Hyperdex for key {} in space {}" , key , space , e );
        }
    }

    @Override
    public void storeAll(Map<K, V> map) {
        map.forEach( (k,v) -> store( k , v ) );
    }

    @Override
    public void delete(K key) {
        try {
            client.del( space , key );
        } catch (HyperDexClientException e) {
            logger.error("Error deleting key {} from hyperdex." , key , e );
        }
    }

    @Override
    public void deleteAll(Collection<K> keys) {
        keys.forEach( key -> delete( key ) );
    }
}
