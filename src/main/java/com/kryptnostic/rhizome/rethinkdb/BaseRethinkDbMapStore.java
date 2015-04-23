package com.kryptnostic.rhizome.rethinkdb;

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

import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dkhenry.RethinkDB.RqlConnection;
import com.dkhenry.RethinkDB.RqlCursor;
import com.dkhenry.RethinkDB.RqlObject;
import com.dkhenry.RethinkDB.RqlQuery.Table;
import com.dkhenry.RethinkDB.errors.RqlDriverException;
import com.geekbeast.rhizome.configuration.hyperdex.MapStoreKeyMapper;
import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.google.common.base.Stopwatch;
import com.hazelcast.core.MapStore;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class BaseRethinkDbMapStore<K, V> implements MapStore<K, V> {
    private static final Logger           logger        = LoggerFactory.getLogger( BaseRethinkDbMapStore.class );
    protected static final String         DATA_FIELD    = "data";
    protected static final String         ID_FIELD      = "id";

    protected static final int            MAX_THREADS   = 16;
    protected static final int            STORAGE_BATCH = 3000;
    protected static final int            LOAD_BATCH    = 3000;

    protected DefaultRethinkDbClientPool  pool;
    protected Table                       tbl;
    protected final ExecutorService       exec          = Executors.newFixedThreadPool( MAX_THREADS );
    protected final MapStoreKeyMapper<K>  keyMapper;
    protected final MapStoreDataMapper<V> mapper;
    protected final String                table;

    public BaseRethinkDbMapStore(
            DefaultRethinkDbClientPool pool,
            String db,
            String table,
            MapStoreKeyMapper<K> keyMapper,
            MapStoreDataMapper<V> mapper ) {
        RqlConnection conn = null;
        this.pool = pool;
        this.table = table;
        this.keyMapper = keyMapper;
        this.mapper = mapper;

        conn = pool.acquire();
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
    }

    private void handleError( RqlDriverException e ) {
        if ( e.getMessage() != null ) {
            logger.error( e.getMessage() );
        } else {
            logger.error( "{}", e );
        }
    }

    public BaseRethinkDbMapStore(
            RethinkDbConfiguration config,
            String db,
            String table,
            MapStoreKeyMapper<K> keyMapper,
            MapStoreDataMapper<V> mapper ) {
        this( new DefaultRethinkDbClientPool( config ), db, table, keyMapper, mapper );
    }

    @Override
    public V load( K key ) {
        RqlConnection conn = pool.acquire();
        try {
            RqlCursor cursor = conn.run( tbl.get( key ) );
            RqlObject obj = cursor.next();
            if ( obj != null && obj.isMap() ) {
                String encoded = (String) obj.getMap().get( DATA_FIELD );
                V val = mapper.fromString( new String( Base64.decodeBase64( encoded ) ) );
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
        List<String> data = Lists.newArrayList();
        for ( K k : rawData ) {
            try {
                data.add( (String) keyMapper.getKey( k ) );
            } catch ( MappingException e ) {
                logger.error( "Failed to map key {}", k, e );
            }
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
            final Object[] arr = data.subList( i, max ).toArray();
            Future<Map<K, V>> t = exec.submit( new Callable<Map<K, V>>() {

                @Override
                public Map<K, V> call() {
                    Map<K, V> results = Maps.newHashMap();
                    Stopwatch watch = Stopwatch.createStarted();
                    RqlConnection conn = pool.acquire();
                    RqlCursor cursor = null;
                    try {
                        cursor = conn.run( tbl.get_all( arr ) );
                    } catch ( RqlDriverException e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    while ( cursor != null && cursor.hasNext() ) {
                        try {
                            RqlObject obj = cursor.next();
                            List<Object> objs = obj.getList();
                            for ( Object o : objs ) {
                                Map<String, Object> d = (Map<String, Object>) o;
                                K key = keyMapper.fromString( (String) d.get( ID_FIELD ) );
                                String encoded = (String) d.get( DATA_FIELD );
                                String str = new String( Base64.decodeBase64( encoded ) );
                                V val = mapper.fromString( str );
                                results.put( key, val );
                                keyCount.incrementAndGet();
                            }
                        } catch ( RqlDriverException | MappingException e ) {
                            errors.incrementAndGet();
                            logger.error( "{}", e );
                        }
                    }
                    pool.release( conn );
                    logger.info(
                            "Retrieval of {} elements took {} ms",
                            arr.length,
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

        System.out.println( "ERRORS: " + errors.get() );
        System.out.println( "KEYCOUNT: " + keyCount.get() );

        return results;
    }

    @Override
    public Set<K> loadAllKeys() {
        return null;
    }

    @Override
    public void store( K key, V value ) {
        RqlConnection conn = null;
        try {
            Object idKey = keyMapper.getKey( key );
            Map<String, Object> data = new HashMap() {
                {
                    put( ID_FIELD, idKey );
                    put( DATA_FIELD, new String( Base64.encodeBase64( mapper.toValueString( value ).getBytes() ) ) );
                }
            };
            Map<String, Object> insertOptions = new HashMap() {
                {
                    put( "conflict", "replace" );
                }
            };
            conn = pool.acquire();
            try {
                conn.run( tbl.insert( data, insertOptions ) );
            } catch ( RqlDriverException e ) {
                logger.error( "Store failed", e );
                throw new RuntimeException( e );
            }
        } catch ( MappingException e ) {
            logger.error( "{}", e );
        } finally {
            if ( conn != null ) {
                pool.release( conn );
            }
        }

    }

    @Override
    public void storeAll( Map<K, V> map ) {
        List<Map<Object, Object>> data = Lists.newArrayList();

        for ( Map.Entry<K, V> entry : map.entrySet() ) {
            try {
                data.add( new HashMap() {
                    {
                        put( ID_FIELD, keyMapper.getKey( entry.getKey() ) );
                        put( DATA_FIELD, Base64.encodeBase64( mapper.toValueString( entry.getValue() ).getBytes() ) );
                    }
                } );
            } catch ( MappingException e ) {
                logger.error( "{}", e );
            }
        }
        long affected = 0;

        Map<String, Object> insertOptions = new HashMap() {
            {
                put( "conflict", "replace" );
            }
        };

        // insert in 100000 chunks
        int sz = data.size();
        int step = STORAGE_BATCH;
        List<Future<Long>> tasks = Lists.newArrayList();
        for ( int i = 0; i < sz; i += step ) {
            int max = i + step;
            if ( max > sz ) {
                max = sz;
            }
            final List list = data.subList( i, max );

            Future<Long> t = exec.submit( new Callable<Long>() {

                @Override
                public Long call() {
                    Stopwatch watch = Stopwatch.createStarted();
                    RqlConnection conn = pool.acquire();
                    long affected = 0;
                    try {
                        RqlCursor cursor = conn.run( tbl.insert( list, insertOptions ) );
                        while ( cursor != null && cursor.hasNext() ) {
                            RqlObject obj = cursor.next();
                            Map m = obj.getMap();
                            affected += (long) (double) m.get( "inserted" );
                        }
                    } catch ( RqlDriverException e ) {
                        logger.error( "StoreAll failed", e );
                    } finally {
                        pool.release( conn );
                    }
                    logger.info( "Insert of {} elements took {} ms", list.size(), watch.elapsed( TimeUnit.MILLISECONDS ) );
                    return affected;
                }
            } );
            tasks.add( t );
        }

        for ( Future<Long> f : tasks ) {
            try {
                affected += f.get();
            } catch ( InterruptedException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch ( ExecutionException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        logger.info( "{} rows affected", affected );
    }

    @Override
    public void delete( K key ) {
        RqlConnection conn = pool.acquire();

        try {
            conn.run( tbl.get( key ).delete() );
        } catch ( RqlDriverException e ) {
            logger.error( "{}", e );
        } finally {
            pool.release( conn );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        RqlConnection conn = pool.acquire();

        try {
            conn.run( tbl.get_all( keys.toArray() ).delete() );
        } catch ( RqlDriverException e ) {
            logger.error( "{}", e );
        } finally {
            pool.release( conn );
        }
    }

}
