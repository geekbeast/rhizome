package com.kryptnostic.rhizome.mapstores;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.hyperdex.MapStoreKeyMapper;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.mappers.MapStoreDataMapper;
import com.kryptnostic.rhizome.rethinkdb.BaseRethinkDbMapStore;
import com.kryptnostic.rhizome.rethinkdb.DefaultRethinkDbClientPool;
import com.rethinkdb.RethinkDBConnection;

public class PreloadingRethinkDbMapStore<K, V> extends BaseRethinkDbMapStore<K, V> {
    private static final Logger logger = LoggerFactory.getLogger( PreloadingRethinkDbMapStore.class );

    public PreloadingRethinkDbMapStore(
            DefaultRethinkDbClientPool pool,
            String db,
            String table,
            MapStoreKeyMapper<K> keyMapper,
            MapStoreDataMapper<V> mapper ) {
        super( pool, db, table, keyMapper, mapper );
    }

    @Override
    public Set<K> loadAllKeys() {
        RethinkDBConnection conn = pool.acquire();
        Set<K> keys = Sets.newHashSet();
        try {
            Object results = tbl.pluck( ID_FIELD ).run( conn );
            // while ( cursor != null && cursor.hasNext() ) {
            // try {
            // RqlObject obj = cursor.next();
            // K key = keyMapper.fromString( (String) obj.getMap().get( ID_FIELD ) );
            // keys.add( key );
            // } catch ( MappingException e ) {
            // logger.error( "{}", e );
            // }
            // }
        } finally {
            if ( conn != null ) {
                pool.release( conn );
            }
        }
        return keys;
    }
}
