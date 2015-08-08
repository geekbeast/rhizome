package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dkhenry.RethinkDB.RqlConnection;
import com.dkhenry.RethinkDB.RqlCursor;
import com.dkhenry.RethinkDB.RqlObject;
import com.dkhenry.RethinkDB.errors.RqlDriverException;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbAlternateDriverClientPool;

public class RethinkDbPreloadingMapStoreAlternateDriver<K, V> extends RethinkDbBaseMapStoreAlternateDriver<K, V> {
    private static final Logger logger = LoggerFactory.getLogger( RethinkDbPreloadingMapStoreAlternateDriver.class );

    public RethinkDbPreloadingMapStoreAlternateDriver(
            RethinkDbAlternateDriverClientPool pool,
            String mapName,
            String db,
            String table,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper ) {
        super( pool, mapName, db, table, keyMapper, mapper );
    }

    @Override
    public Set<K> loadAllKeys() {
        RqlConnection conn = pool.acquire();
        Set<K> keys = Sets.newHashSet();
        try {
            RqlCursor cursor = conn.run( tbl.pluck( ID_FIELD ) );
            while ( cursor != null && cursor.hasNext() ) {
                try {
                    RqlObject obj = cursor.next();
                    String rawData = (String) obj.getMap().get( ID_FIELD );
                    K key = keyMapper.toKey( rawData );
                    keys.add( key );
                } catch ( RqlDriverException | MappingException e ) {
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
}
