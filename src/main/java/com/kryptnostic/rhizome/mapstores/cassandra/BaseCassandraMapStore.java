package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.collect.Maps;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
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

    protected final String          mapName;
    protected final SelfRegisteringValueMapper<V> valueMapper;
    protected final SelfRegisteringKeyMapper<K>   keyMapper;
    protected final Session         session;
    protected final String          table;
    final String                    keyspace;

    final int                      replicationFactor;

    public BaseCassandraMapStore(
            String table,
            String mapName,
            SelfRegisteringKeyMapper<K> keyMapper,
            SelfRegisteringValueMapper<V> mapper,
            CassandraConfiguration config,
            Session globalSession ) {
        this.table = table;
        this.keyMapper = keyMapper;
        this.valueMapper = mapper;
        this.session = globalSession;
        this.mapName = mapName;
        this.replicationFactor = config.getReplicationFactor();

        keyspace = config.getKeyspace();

    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        return Maps.toMap( keys, ( K key ) -> {
            return load( key );
        } );
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        for ( Entry<K, V> ent : map.entrySet() ) {
            store( ent.getKey(), ent.getValue() );
        }
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return new MapStoreConfig().setImplementation( this ).setEnabled( true ).setWriteDelaySeconds( 0 );
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
}
