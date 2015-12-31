package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dkhenry.RethinkDB.RqlConnection;
import com.dkhenry.RethinkDB.RqlCursor;
import com.dkhenry.RethinkDB.RqlObject;
import com.dkhenry.RethinkDB.RqlQuery.Table;
import com.dkhenry.RethinkDB.errors.RqlDriverException;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.kryptnostic.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbAlternateDriverClientPool;

public abstract class RethinkDbBaseMapStoreAlternateDriver<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    private static final Base64                        codec                  = new Base64();
    private static final Logger                        logger                 = LoggerFactory
                                                                                      .getLogger( RethinkDbBaseMapStoreAlternateDriver.class );
    protected static final String                      DATA_FIELD             = "data";
    protected static final String                      ID_FIELD               = "id";

    protected static final int                         MAX_THREADS            = Runtime.getRuntime()
                                                                                      .availableProcessors();
    protected static final int                         STORAGE_BATCH          = 3000;
    protected static final int                         LOAD_BATCH             = 3000;

    protected static final ExecutorService             exec                   = Executors
                                                                                      .newFixedThreadPool( MAX_THREADS );

    protected final RethinkDbAlternateDriverClientPool pool;
    protected final Table                              tbl;
    protected final KeyMapper<K>                       keyMapper;
    protected final ValueMapper<V>                     mapper;
    protected final String                             table;
    protected final String                             mapName;

    public static final HashMap<String, Object>        INSERT_OPTIONS         = new HashMap<String, Object>() {
                                                                                  {
                                                                                      put( "conflict", "replace" );
                                                                                  }
                                                                              };

    public RethinkDbBaseMapStoreAlternateDriver(
            RethinkDbAlternateDriverClientPool pool,
            String mapName,
            String db,
            String table,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        this.pool = pool;
        this.mapName = mapName;
        this.table = table;
        this.keyMapper = keyMapper;
        this.mapper = mapper;

        RqlConnection conn = pool.acquire();
        boolean dbExists = false;
        boolean tableExists = false;
        try {
            // check if db exists
            RqlCursor cursor = conn.run( conn.db_list() );
            if ( cursor != null && cursor.hasNext() ) {
                RqlObject obj = cursor.next();
                List<String> list = Lists.newArrayList( obj.getList().toArray( new String[] {} ) );
                dbExists = list.contains( db );
            }
            if ( !dbExists ) {
                conn.run( conn.db_create( db ) );
            }
            // check for table existence
            cursor = conn.run( conn.db( db ).table_list() );
            if ( cursor != null && cursor.hasNext() ) {
                RqlObject obj = cursor.next();
                List<String> list = Lists.newArrayList( obj.getList().toArray( new String[] {} ) );
                tableExists = list.contains( table );
            }
            if ( !tableExists ) {
                conn.run( conn.db( db ).table_create( table ) );
            }
        } catch ( RqlDriverException e ) {
            handleError( e );
        }

        // close connection
        tbl = conn.db( db ).table( table );
        pool.release( conn );
        RegistryBasedHazelcastInstanceConfigurationPod.register( mapName, this );
    }

    public RethinkDbBaseMapStoreAlternateDriver(
            RethinkDbConfiguration config,
            String mapName,
            String db,
            String table,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        this( new RethinkDbAlternateDriverClientPool( config ), mapName, db, table, keyMapper, mapper );
    }

    private void handleError( RqlDriverException e ) {
        if ( e.getMessage() != null ) {
            logger.error( e.getMessage() );
        } else {
            logger.error( "{}", e );
        }
    }

    @Override
    public V load( K key ) {
        RqlConnection conn = pool.acquire();
        try {
            String keyString = keyMapper.fromKey( key );
            RqlCursor cursor = conn.run( tbl.get( keyString ) );
            RqlObject obj = cursor.next();
            if ( obj != null && obj.isMap() ) {
                V val = RethinkDbBaseMapStoreAlternateDriver.this.getValueFromCursorObject( obj );
                return val;
            }
        } catch ( RqlDriverException | MappingException e ) {
            logger.error( "{}", e );
        } finally {
            pool.release( conn );
        }
        return null;
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> results = Maps.newConcurrentMap();

        int sz = keys.size();
        List<K> rawData = Lists.newArrayList( keys );
        final List<Object> data = Lists.newArrayList();
        for ( K k : rawData ) {
            data.add( keyMapper.fromKey( k ) );
        }
        int step = LOAD_BATCH;
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger keyCount = new AtomicInteger();
        List<Future> tasks = Lists.newArrayList();
        for ( int i = 0; i < sz; i += step ) {
            int max = i + step;
            if ( max > sz ) {
                max = sz;
            }
            final int fIndex = i;
            final int fMax = max;
            Future<Map<K, V>> t = exec.submit( new Callable<Map<K, V>>() {

                @Override
                public Map<K, V> call() {
                    Map<K, V> results = Maps.newHashMap();
                    Stopwatch watch = Stopwatch.createStarted();

                    RqlConnection conn = pool.acquire();
                    RqlCursor cursor = null;
                    List<Object> subListOfData = data.subList( fIndex, fMax );
                    try {
                        cursor = conn.run( tbl.get_all( subListOfData.toArray() ) );
                    } catch ( RqlDriverException e1 ) {
                        logger.error( "{}", e1 );
                    }
                    while ( cursor != null && cursor.hasNext() ) {
                        try {
                            RqlObject obj = cursor.next();
                            Map<String, Object> d = obj.getMap();
                            K key = keyMapper.toKey( (String) d.get( ID_FIELD ) );
                            V val = RethinkDbBaseMapStoreAlternateDriver.this.getValueFromCursorObject( obj );
                            results.put( key, val );
                            keyCount.incrementAndGet();
                        } catch ( RqlDriverException | MappingException e ) {
                            errors.incrementAndGet();
                            logger.error( "{}", e );
                        }
                    }
                    pool.release( conn );
                    logger.info(
                            "{} Retrieval of {} elements took {} ms",
                            table,
                            fMax - fIndex,
                            watch.elapsed( TimeUnit.MILLISECONDS ) );
                    return results;
                }

            } );
            tasks.add( t );
        }

        for ( Future f : tasks ) {
            try {
                results.putAll( (Map<K, V>) f.get() );
            } catch ( InterruptedException e ) {
                logger.error( "{}", e );
            } catch ( ExecutionException e ) {
                logger.error( "{}", e );
            }
        }

        logger.debug( "ERRORS: " + errors.get() );
        logger.debug( "KEYCOUNT: " + keyCount.get() );

        return results;
    }

    @Override
    public Set<K> loadAllKeys() {
        return null;
    }

    private V getValueFromCursorObject( RqlObject obj ) throws MappingException, RqlDriverException {
        String encoded = (String) obj.getMap().get( DATA_FIELD );
        if ( encoded == null ) {
            return null;
        }
        byte[] decodedBytes = codec.decodeBase64( encoded );
        V val = mapper.fromBytes( decodedBytes );
        return val;
    }

    private Object prepareValueForStorage( V value ) throws MappingException {
        byte[] bytes = mapper.toBytes( value );
        String payload = new String( codec.encodeBase64( bytes ) );
        return payload;
    }

    @Override
    public void store( K key, V value ) {
        RqlConnection conn = null;
        try {
            Object idKey = keyMapper.fromKey( key );
            Object valuePayload = prepareValueForStorage( value );

            Map<String, Object> data = new HashMap<String, Object>() {
                {
                    put( ID_FIELD, idKey );
                    put( DATA_FIELD, valuePayload );
                }
            };

            conn = pool.acquire();
            try {
                conn.run( tbl.insert( data ), INSERT_OPTIONS );
            } catch ( RqlDriverException e ) {
                logger.error( "Store failed", e );
            }

        } catch ( MappingException e ) {
            logger.error( "Failed to map value or key {}", e );
        } finally {
            if ( conn != null ) {
                pool.release( conn );
            }
        }

    }

    @Override
    public void storeAll( Map<K, V> map ) {
        List<Map.Entry<K, V>> data = Lists.newArrayList( map.entrySet() );

        long affected = 0;

        // insert in 100000 chunks
        int sz = data.size();
        int step = STORAGE_BATCH;
        List<Future<Long>> tasks = Lists.newArrayList();
        for ( int i = 0; i < sz; i += step ) {
            int max = i + step;
            if ( max > sz ) {
                max = sz;
            }
            final int finalIndex = i;
            final int finalMax = max;
            Future<Long> t = exec.submit( new Callable<Long>() {

                @Override
                public Long call() {
                    Stopwatch watch = Stopwatch.createStarted();
                    RqlConnection conn = pool.acquire();
                    long affected = 0;
                    try {
                        List<Map<String, Object>> toInsert = Lists.newArrayList();
                        final List<Map.Entry<K, V>> list = data.subList( finalIndex, finalMax );
                        for ( Map.Entry<K, V> entry : list ) {
                            try {
                                toInsert.add( new HashMap<String, Object>() {
                                    {
                                        put( ID_FIELD, keyMapper.fromKey( entry.getKey() ) );
                                        put( DATA_FIELD, prepareValueForStorage( entry.getValue() ) );
                                    }
                                } );
                            } catch ( MappingException e ) {
                                logger.error( "{}", e );
                            }
                        }

                        RqlCursor cursor = conn.run( tbl.insert( toInsert.toArray() ), INSERT_OPTIONS );
                        while ( cursor != null && cursor.hasNext() ) {
                            RqlObject obj = cursor.next();
                            Map m = obj.getMap();
                            affected += (long) m.get( "inserted" );
                        }

                    } catch ( RqlDriverException e ) {
                        logger.error( "StoreAll failed", e );
                    } finally {
                        pool.release( conn );
                    }
                    logger.debug(
                            "{} Insert of {} elements took {} ms",
                            table,
                            ( finalMax - finalIndex ),
                            watch.elapsed( TimeUnit.MILLISECONDS ) );
                    return affected;
                }
            } );
            tasks.add( t );
        }

        for ( Future<Long> f : tasks ) {
            try {
                affected += f.get();
            } catch ( InterruptedException | ExecutionException e ) {
                // TODO log here instead?
                e.printStackTrace();
            }
        }

        logger.info( "{} rows affected", affected );
    }

    @Override
    public void delete( K key ) {
        RqlConnection conn = pool.acquire();

        try {
            String keyString = keyMapper.fromKey( key );
            conn.run( tbl.get( keyString ).delete() );
        } catch ( RqlDriverException e ) {
            logger.error( "Failed to delete key {} of type {}", key, key.getClass().getCanonicalName() );
        } finally {
            pool.release( conn );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        RqlConnection conn = pool.acquire();
        try {
            List<Object> stringKeys = Lists.newArrayList();
            for ( K k : keys ) {
                    String keyString = keyMapper.fromKey( k );
                    stringKeys.add( keyString );
            }
            conn.run( tbl.get_all( stringKeys.toArray() ).delete() );

        } catch ( RqlDriverException e ) {
            logger.error( "Failed to delete all keys {}", keys, e );
        } finally {
            pool.release( conn );
        }
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return new MapStoreConfig().setImplementation( this ).setEnabled( true ).setWriteDelaySeconds( 0 );
    }

    @Override
    public MapConfig getMapConfig() {
        return new MapConfig( mapName ).setBackupCount( 0 ).setMapStoreConfig( getMapStoreConfig() );
    }

    @Override
    public String getTable() {
        return this.table;
    }

    @Override
    public String getMapName() {
        return this.mapName;
    }
}
