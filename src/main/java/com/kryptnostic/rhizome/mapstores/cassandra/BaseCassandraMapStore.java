package com.kryptnostic.rhizome.mapstores.cassandra;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.collect.Sets;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

import jersey.repackaged.com.google.common.collect.Iterables;
import jersey.repackaged.com.google.common.collect.Maps;

public abstract class BaseCassandraMapStore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    private static final Logger        logger         = LoggerFactory.getLogger( BaseCassandraMapStore.class );
    private static final String        KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String     TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (id text PRIMARY KEY, data blob);";
    protected final String          mapName;
    protected final ValueMapper<V>  valueMapper;
    protected final KeyMapper<K>       keyMapper;

    private final PreparedStatement    LOAD_QUERY;
    private final PreparedStatement    STORE_QUERY;
    private final PreparedStatement    DELETE_QUERY;
    private final PreparedStatement    LOAD_ALL_QUERY;
    private final PreparedStatement    DELETE_ALL_QUERY;
    protected final Session         session;
    private final int replicationFactor;
    protected final String          table;

    public BaseCassandraMapStore(
            String table,
            String mapName,
            KeyMapper<K> keyMapper,
            ValueMapper<V> mapper,
            CassandraConfiguration config,
            Session globalSession ) {
        this.table = table;
        this.keyMapper = keyMapper;
        this.valueMapper = mapper;
        this.session = globalSession;
        this.mapName = mapName;
        this.replicationFactor = config.getReplicationFactor();

        // Find a better place for this. It shouldn't be here.
        // Metadata metadata = cluster.getMetadata();
        // logger.info( "Connected to cluster: {}", metadata.getClusterName() );
        // for ( Host host : metadata.getAllHosts() ) {
        // logger.info(
        // "Datacenter: {}; Host: {}; Rack: {}\n",
        // host.getDatacenter(),
        // host.getAddress(),
        // host.getRack() );
        // }
        String keyspace = config.getKeyspace();
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
            ByteBuffer bytes = result.getBytes( "data" );
            return valueMapper.fromBytes( bytes.array() );
        } catch ( MappingException e ) {
            logger.error( "Unable to map key to cassandra key.", e );
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
            session.execute( STORE_QUERY.bind( keyMapper.fromKey( key ), valueMapper.toBytes( value ) ) );
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
        session.execute( DELETE_QUERY.bind( keyMapper.fromKey( key ) ) );
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
