package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.collect.Sets;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.cassandra.CassandraMapper;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

import jersey.repackaged.com.google.common.collect.Iterables;
import jersey.repackaged.com.google.common.collect.Maps;

public abstract class BaseCassandraMapStore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    private final Cluster              cluster;
    private static final Logger        logger         = LoggerFactory.getLogger( BaseCassandraMapStore.class );
    private static final String        KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String        TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (id text PRIMARY KEY, data text);";
    private final String                mapName;
    protected final CassandraMapper<V> mapper;
    protected final KeyMapper<K>       keyMapper;

    private final PreparedStatement    LOAD_QUERY;
    private final PreparedStatement    STORE_QUERY;
    private final PreparedStatement    DELETE_QUERY;
    private final PreparedStatement    LOAD_ALL_QUERY;
    private final PreparedStatement    DELETE_ALL_QUERY;
    private final Session              session;
    private final int replicationFactor;
    private final String               table;

    public BaseCassandraMapStore(
            String table,
            String mapName,
            KeyMapper<K> keyMapper,
            CassandraMapper<V> mapper,
            CassandraConfiguration config,
            Cluster globalCluster) {
        this.table = table;
        this.keyMapper = keyMapper;
        this.mapper = mapper;
        this.cluster = globalCluster;
        this.mapName = mapName;
        this.replicationFactor = config.getReplicationFactor();

        Metadata metadata = cluster.getMetadata();
        logger.info( "Connected to cluster: {}", metadata.getClusterName() );
        for ( Host host : metadata.getAllHosts() ) {
            logger.info(
                    "Datacenter: {}; Host: {}; Rack: {}\n",
                    host.getDatacenter(),
                    host.getAddress(),
                    host.getRack() );
        }
        String keyspace = config.getKeyspace();
        session = cluster.newSession();
        session.execute( String.format( KEYSPACE_QUERY, keyspace, replicationFactor ) );
        session.execute( String.format( TABLE_QUERY, keyspace, table ) );

        LOAD_QUERY = session.prepare( QueryBuilder.select( "data" ).from( keyspace, table )
                .where( QueryBuilder.eq( "id", QueryBuilder.bindMarker() ) ) );
        STORE_QUERY = session.prepare( QueryBuilder.insertInto( keyspace, table )
                .value( "id", QueryBuilder.bindMarker() ).value( "data", QueryBuilder.bindMarker() ) );
        DELETE_QUERY = session.prepare( QueryBuilder.delete().from( keyspace, table )
                .where( QueryBuilder.eq( "id", QueryBuilder.bindMarker() ) ) );
        LOAD_ALL_QUERY = session.prepare( QueryBuilder.select( "id" ).from( keyspace, table ) );
        DELETE_ALL_QUERY = session.prepare( QueryBuilder.delete().from( keyspace, table )
                .where( QueryBuilder.in( "id", QueryBuilder.bindMarker() ) ) );

    }

    @Override
    public V load( K key ) {
        ResultSet s;
        try {
            s = session.execute( LOAD_QUERY.bind( keyMapper.fromKey( key ) ) );
            Row result = s.one();
            if ( result == null ) {
                return null;
            }
            return mapper.map( result.getString( "data" ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to map key to cassandra key." );
        }
        return null;
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        return Maps.toMap( keys, ( K key ) -> {
            return load( key );
        } );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Set<K> loadAllKeys() {
        ResultSet s;
        s = session.execute( LOAD_ALL_QUERY.bind() );
        return Sets.newHashSet( Iterables.transform( s.all(), ( Row r ) -> {
            return (K) r.getString( "id" );
        } ) );
    }

    @Override
    public void store( K key, V value ) {
        try {
            session.execute( STORE_QUERY.bind( keyMapper.fromKey( key ), mapper.asString( value ) ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to store key {} : value {} ", key, value, e );
        }
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        for ( Entry<K, V> ent : map.entrySet() ) {
            store( ent.getKey(), ent.getValue() );
        }
    }

    @Override
    public void delete( K key ) {
        try {
            session.execute( DELETE_QUERY.bind( keyMapper.fromKey( key ) ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to delete key {} : value {} ", key );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        session.execute( DELETE_ALL_QUERY.bind( keys ) );
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
