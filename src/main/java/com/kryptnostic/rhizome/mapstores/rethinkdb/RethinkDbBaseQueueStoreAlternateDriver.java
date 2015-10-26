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
import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.QueueStoreConfig;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringQueueStore;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbAlternateDriverClientPool;

public class RethinkDbBaseQueueStoreAlternateDriver<T> implements SelfRegisteringQueueStore<T> {
    private static final Base64                        codec          = new Base64();
    private static final Logger                        logger         = LoggerFactory
                                                                              .getLogger( RethinkDbBaseQueueStoreAlternateDriver.class );
    protected static final String                      DATA_FIELD     = "data";
    protected static final String                      ID_FIELD       = "id";

    protected static final int                         MAX_THREADS    = Runtime.getRuntime().availableProcessors();
    protected static final int                         STORAGE_BATCH  = 3000;
    protected static final int                         LOAD_BATCH     = 3000;

    protected static final ExecutorService             exec           = Executors.newFixedThreadPool( MAX_THREADS );

    protected final RethinkDbAlternateDriverClientPool pool;
    protected final Table                              tbl;
    protected final ValueMapper<T>                     mapper;
    protected final String                             table;
    protected final String                             queueName;

    public static final HashMap<String, Object>        INSERT_OPTIONS = new HashMap<String, Object>() {
                                                                          {
                                                                              put( "conflict", "replace" );
                                                                          }
                                                                      };

    public RethinkDbBaseQueueStoreAlternateDriver(
            RethinkDbAlternateDriverClientPool pool,
            String db,
            String table,
            String queueName,
            ValueMapper<T> mapper ) {
        RqlConnection conn = null;
        this.pool = pool;
        this.table = table;
        this.mapper = mapper;
        this.queueName = queueName;

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

    public RethinkDbBaseQueueStoreAlternateDriver(
            RethinkDbConfiguration config,
            String db,
            String table,
            String queueName,
            ValueMapper<T> mapper ) {
        this( new RethinkDbAlternateDriverClientPool( config ), db, table, queueName, mapper );
    }

    @Override
    public T load( Long key ) {
        RqlConnection conn = pool.acquire();
        try {
            RqlCursor cursor = conn.run( tbl.get( key ) );
            RqlObject obj = cursor.next();
            if ( obj != null && obj.isMap() ) {
                T val = RethinkDbBaseQueueStoreAlternateDriver.this.getValueFromCursorObject( obj );
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
    public Map<Long, T> loadAll( Collection<Long> keys ) {
        Map<Long, T> results = Maps.newConcurrentMap();

        int sz = keys.size();
        List<Long> data = Lists.newArrayList( keys );
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
            Future<Map<Long, T>> t = exec.submit( new Callable<Map<Long, T>>() {

                @Override
                public Map<Long, T> call() {
                    Map<Long, T> results = Maps.newHashMap();
                    Stopwatch watch = Stopwatch.createStarted();

                    RqlConnection conn = pool.acquire();
                    RqlCursor cursor = null;
                    List<Long> subListOfData = data.subList( fIndex, fMax );
                    try {
                        cursor = conn.run( tbl.get_all( subListOfData.toArray() ) );
                    } catch ( RqlDriverException e1 ) {
                        logger.error( "{}", e1 );
                    }
                    while ( cursor != null && cursor.hasNext() ) {
                        try {
                            RqlObject obj = cursor.next();
                            Map<String, Object> d = obj.getMap();
                            Long key = ( (Double) d.get( ID_FIELD ) ).longValue();
                            T val = RethinkDbBaseQueueStoreAlternateDriver.this.getValueFromCursorObject( obj );
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
                results.putAll( (Map<Long, T>) f.get() );
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
    public Set<Long> loadAllKeys() {
        RqlConnection conn = pool.acquire();
        Set<Long> keys = Sets.newHashSet();
        try {
            RqlCursor cursor = conn.run( tbl.pluck( ID_FIELD ) );
            while ( cursor != null && cursor.hasNext() ) {
                try {
                    RqlObject obj = cursor.next();
                    Double key = (Double) obj.getMap().get( ID_FIELD );
                    keys.add( key.longValue() );
                } catch ( RqlDriverException e ) {
                    logger.error( "{}", e );
                }
            }
        } catch ( RqlDriverException e ) {
            logger.error( "{}", e );
        } finally {
            if ( conn != null ) {
                pool.release( conn );
            }
        }
        return keys;

    }

    private T getValueFromCursorObject( RqlObject obj ) throws MappingException, RqlDriverException {
        String encoded = (String) obj.getMap().get( DATA_FIELD );
        if ( encoded == null ) {
            return null;
        }
        byte[] decodedBytes = codec.decodeBase64( encoded );
        T val = mapper.fromBytes( decodedBytes );
        return val;
    }

    private Object prepareValueForStorage( T value ) throws MappingException {
        byte[] bytes = mapper.toBytes( value );
        String payload = new String( codec.encodeBase64( bytes ) );
        return payload;
    }

    @Override
    public void store( Long key, T value ) {
        RqlConnection conn = null;
        try {
            Object valuePayload = prepareValueForStorage( value );

            Map<String, Object> data = new HashMap<String, Object>() {
                {
                    put( ID_FIELD, key );
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
    public void storeAll( Map<Long, T> map ) {
        List<Map.Entry<Long, T>> data = Lists.newArrayList( map.entrySet() );

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
                        final List<Map.Entry<Long, T>> list = data.subList( finalIndex, finalMax );
                        for ( Map.Entry<Long, T> entry : list ) {
                            try {
                                toInsert.add( new HashMap<String, Object>() {
                                    {
                                        put( ID_FIELD, entry.getKey() );
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
                            affected += (long) (double) m.get( "inserted" );
                        }

                    } catch ( RqlDriverException e ) {
                        logger.error( "StoreAll failed", e );
                    } finally {
                        pool.release( conn );
                    }
                    logger.info(
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
    public void delete( Long key ) {
        RqlConnection conn = pool.acquire();

        try {
            conn.run( tbl.get( key ).delete() );
        } catch ( RqlDriverException e ) {
            logger.error( "Failed to delete key {} of type {}", key, key.getClass().getCanonicalName() );
        } finally {
            pool.release( conn );
        }
    }

    @Override
    public void deleteAll( Collection<Long> keys ) {
        RqlConnection conn = pool.acquire();
        try {
            List<Object> stringKeys = Lists.newArrayList();
            for ( Long k : keys ) {
                stringKeys.add( k );
            }
            conn.run( tbl.get_all( stringKeys.toArray() ).delete() );

        } catch ( RqlDriverException e ) {
            logger.error( "Failed to delete all keys {}", keys, e );
        } finally {
            pool.release( conn );
        }
    }

    @Override
    public QueueConfig getQueueConfig() {
        return new QueueConfig().setBackupCount( 2 )
                .setQueueStoreConfig( new QueueStoreConfig().setStoreImplementation( this ) ).setName( queueName );
    }

}
