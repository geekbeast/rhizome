package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

/**
 * Base cassandra-backed mapstore. This class should not execute any queries directly
 *
 * @author Drew Bailey drew@kryptnostic.com
 * @author Matthew Tamayo-Rios matthew@kryptnostic.com
 *
 * @param <K>
 * @param <V>
 */
public abstract class BaseCassandraMapStore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    private static final Logger        logger         = LoggerFactory.getLogger( BaseCassandraMapStore.class );

    static final String            DEFAULT_KEY_COLUMN_NAME   = "id";
    static final String            DEFAULT_VALUE_COLUMN_NAME = "data";
    protected static final int                    DEFAULT_QUERY_LIMIT       = 10_000;

    protected final String          mapName;
    protected final SelfRegisteringValueMapper<V> valueMapper;
    protected final SelfRegisteringKeyMapper<K>   keyMapper;
    protected final Session         session;
    protected final String          table;
    protected final String                        keyspace;
    protected final int                           replicationFactor;
    protected boolean                             SUPPORTS_ASYNC_LOADS      = true;

    public BaseCassandraMapStore(
            String table,
            String mapName,
            SelfRegisteringKeyMapper<K> keyMapper,
            SelfRegisteringValueMapper<V> mapper,
            CassandraConfiguration config,
            Session session ) {
        this.table = table;
        this.keyMapper = keyMapper;
        this.valueMapper = mapper;
        this.session = session;
        this.mapName = mapName;
        this.replicationFactor = config.getReplicationFactor();
        this.keyspace = config.getKeyspace();
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        if ( !SUPPORTS_ASYNC_LOADS ) {
            return defaultLoadAll( keys );
        }
        return asyncLoadAll( keys );
    }

    private Map<K, V> asyncLoadAll( Collection<K> keys ) {
        ConcurrentMap<K, V> resultMap = new MapMaker()
                .initialCapacity( keys.size() )
                .concurrencyLevel( 8 )
                .<K, V> makeMap();

        CountDownLatch latch = new CountDownLatch( keys.size() );
        for ( K key : keys ) {
            Futures.addCallback( asyncLoad( key ), new FutureCallback<ResultSet>() {
                @Override
                public void onSuccess( ResultSet results ) {
                    for ( Row row : results ) {
                        resultMap.put( key, mapToValue( row ) );
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure( Throwable t ) {
                    System.err.println( "Asynchronous cassandra queries failed" );
                    t.printStackTrace();
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch ( InterruptedException e ) {
            System.err.println( "Async queries timed out." );
            e.printStackTrace();
        }

        return resultMap;
    }

    private Map<K, V> defaultLoadAll( Collection<K> keys ) {
        // Naive implementation for now :(
        Map<K, V> result = Maps.newHashMap();
        for (K key : keys) {
            V value = load( key );
            if (value != null) {
                result.put( key, value );
            }
        }
        return result;
    }

    protected abstract ResultSetFuture asyncLoad( K key );

    protected abstract K mapToKey( Row row );

    protected abstract V mapToValue( Row row );

    @Override
    public void storeAll( Map<K, V> map ) {
        for ( Entry<K, V> ent : map.entrySet() ) {
            store( ent.getKey(), ent.getValue() );
        }
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return new MapStoreConfig().setImplementation( this ).setEnabled( true )
                .setWriteDelaySeconds( 0 );
    }

    @Override
    public MapConfig getMapConfig() {
        return new MapConfig( mapName ).setBackupCount( this.replicationFactor ).setMapStoreConfig( getMapStoreConfig() );
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public K generateTestKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED. Override this method in your subclass!" );
    }

    @Override
    public V generateTestValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED. Override this method in your subclass!" );
    }
}
