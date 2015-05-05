package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbDefaultClientPool;
import com.rethinkdb.RethinkDBConnection;

public class RethinkDbPreloadingMapStore<K, V> extends RethinkDbBaseMapStore<K, V> {
    private static final Logger logger = LoggerFactory.getLogger( RethinkDbPreloadingMapStore.class );

    public RethinkDbPreloadingMapStore(
            RethinkDbDefaultClientPool pool,
            String db,
            String table,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        super( pool, db, table, keyMapper, mapper );
    }

    @Override
    public Set<K> loadAllKeys() {
        RethinkDBConnection conn = pool.acquire();
        Set<K> keys = Sets.newHashSet();
        try {
            Object results = tbl.pluck( ID_FIELD ).run( conn );
            results.getClass();
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
