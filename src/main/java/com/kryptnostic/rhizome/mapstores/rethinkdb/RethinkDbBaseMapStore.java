package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.Collection;
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

import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.google.common.base.Stopwatch;
import com.hazelcast.core.MapStore;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbDefaultClientPool;
import com.rethinkdb.Cursor;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.RethinkDBConnection;
import com.rethinkdb.ast.query.gen.Table;
import com.rethinkdb.model.ConflictStrategy;
import com.rethinkdb.model.Durability;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.response.model.DMLResult;

public class RethinkDbBaseMapStore<K, V> implements MapStore<K, V> {
    private static final Logger          logger        = LoggerFactory.getLogger( RethinkDbBaseMapStore.class );
    protected static final String        DATA_FIELD    = "data";
    protected static final String        ID_FIELD      = "id";

    protected static final int           MAX_THREADS   = 16;
    protected static final int           STORAGE_BATCH = 3000;
    protected static final int           LOAD_BATCH    = 3000;

    protected RethinkDbDefaultClientPool pool;
    protected Table                      tbl;
    protected final ExecutorService      exec          = Executors.newFixedThreadPool( MAX_THREADS );
    protected final KeyMapper<K>         keyMapper;
    protected final ValueMapper<V>       mapper;
    protected final String               table;
    protected static final RethinkDB     r             = RethinkDB.r;

    public RethinkDbBaseMapStore(
            RethinkDbDefaultClientPool pool,
            String db,
            String table,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        RethinkDBConnection conn = null;
        this.pool = pool;
        this.table = table;
        this.keyMapper = keyMapper;
        this.mapper = mapper;

        conn = pool.acquire();

        List<String> databases = r.dbList().run( conn );
        if ( !databases.contains( db ) ) {
            r.dbCreate( db ).run( conn );
        }
        List<String> tables = r.db( db ).tableList().run( conn );
        if ( !tables.contains( table ) ) {
            r.db( db ).tableCreate( table ).run( conn );
        }

        tbl = r.db( db ).table( table );
        pool.release( conn );
    }

    public RethinkDbBaseMapStore(
            RethinkDbConfiguration config,
            String db,
            String table,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        this( new RethinkDbDefaultClientPool( config ), db, table, keyMapper, mapper );
    }

    @Override
    public V load( K key ) {
        RethinkDBConnection conn = pool.acquire();
        try {

            Map<String, Object> objects = tbl.get( keyMapper.fromKey( key ) ).run( conn );

            if ( objects == null ) {
                return null;
            }

            Object rawValue = objects.get( DATA_FIELD );

            if ( rawValue == null ) {
                return null;
            }

            V val = mapper.fromBytes( (Map) rawValue );

            return val;

        } catch ( MappingException e ) {
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
            try {
                data.add( keyMapper.fromKey( k ) );
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
            final int fIndex = i;
            final int fMax = max;
            Future<Map<K, V>> t = exec.submit( new Callable<Map<K, V>>() {

                @Override
                public Map<K, V> call() {
                    Map<K, V> results = Maps.newHashMap();
                    Stopwatch watch = Stopwatch.createStarted();
                    RethinkDBConnection conn = pool.acquire();
                    List<Object> subListOfData = data.subList( fIndex, fMax );
                    Cursor<Map<String, Object>> cursor = tbl.getAll( subListOfData ).run( conn );
                    while ( cursor != null && cursor.hasNext() ) {
                        try {
                            Map<String, Object> obj = cursor.next();

                            K key = keyMapper.toKey( (String) obj.get( ID_FIELD ) );
                            Map<String, Object> encoded = (Map<String, Object>) obj.get( DATA_FIELD );
                            String str = new String( Base64.decodeBase64( (String) encoded.get( DATA_FIELD ) ) );
                            V val = mapper.toKey( str );
                            results.put( key, val );
                            keyCount.incrementAndGet();

                        } catch ( MappingException e ) {
                            errors.incrementAndGet();
                            logger.error( "{}", e );
                        }
                    }
                    pool.release( conn );
                    logger.info(
                            "Retrieval of {} elements took {} ms",
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
        RethinkDBConnection conn = null;
        try {
            Object idKey = keyMapper.fromKey( key );
            conn = pool.acquire();

            byte[] val = Base64.encodeBase64( mapper.toValueString( value ).getBytes() );
            MapObject binaryData = new MapObject().with( "$reql_type$", "BINARY" ).with( "data", val );
            tbl.insert(
                    new MapObject().with( ID_FIELD, idKey ).with( DATA_FIELD, binaryData ),
                    Durability.hard,
                    false,
                    ConflictStrategy.replace ).run( conn );

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
        List<Map<String, Object>> data = Lists.newArrayList();

        for ( Map.Entry<K, V> entry : map.entrySet() ) {
            try {

                byte[] val = Base64.encodeBase64( mapper.toValueString( entry.getValue() ).getBytes() );
                String idKey = (String) keyMapper.fromKey( entry.getKey() );
                MapObject binaryData = new MapObject().with( "$reql_type$", "BINARY" ).with( "data", val );
                data.add( new MapObject().with( ID_FIELD, idKey ).with( DATA_FIELD, binaryData ) );

            } catch ( MappingException e ) {
                logger.error( "{}", e );
            }
        }
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
            final List<Map<String, Object>> list = data.subList( i, max );

            Future<Long> t = exec.submit( new Callable<Long>() {

                @Override
                public Long call() {
                    Stopwatch watch = Stopwatch.createStarted();
                    RethinkDBConnection conn = pool.acquire();
                    long affected = 0;
                    try {
                        DMLResult results = tbl.insert( list, Durability.hard, true, ConflictStrategy.replace ).run(
                                conn );

                        affected += results.getInserted();
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
        RethinkDBConnection conn = pool.acquire();

        try {
            tbl.get( key ).delete().run( conn );
        } finally {
            pool.release( conn );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        RethinkDBConnection conn = pool.acquire();

        try {
            tbl.getAll( (List<Object>) keys ).delete().run( conn );
        } finally {
            pool.release( conn );
        }
    }

}
