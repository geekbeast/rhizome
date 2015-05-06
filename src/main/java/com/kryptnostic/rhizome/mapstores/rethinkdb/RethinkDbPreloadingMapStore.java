package com.kryptnostic.rhizome.mapstores.rethinkdb;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
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
            if ( results instanceof List ) {
                List<Object> resultList = (List<Object>) results;
                for ( Object res : resultList ) {
                    Map<String, Object> resultMap = (Map<String, Object>) res;
                    String resKey = (String) resultMap.get( KeyMapper.ID_ATTRIBUTE );
                    try {
                        K key = keyMapper.toKey( resKey );
                        keys.add( key );
                    } catch ( MappingException e ) {
                        logger.error( "Could not map key from RethinkDb {} {}", e, new TypeReference<K>() {}.getClass() );
                    }
                }
            }
        } finally {
            if ( conn != null ) {
                pool.release( conn );
            }
        }
        return keys;
    }
}
