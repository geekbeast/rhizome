package com.kryptnostic.rhizome.cassandra;

import java.util.Collection;
import java.util.Map;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.core.MapStore;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class BaseCassandraMapStore<K, V> implements MapStore<K, V> {
    private final Cluster              cluster;
    private static final Logger        logger         = LoggerFactory.getLogger( BaseCassandraMapStore.class );
    private static final String        KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':2};";
    private static final String        TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (id text PRIMARY KEY, data text);";
    protected final CassandraMapper<V> mapper;
    protected final KeyMapper<K>       keyMapper;

    private final PreparedStatement    LOAD_QUERY;
    private final PreparedStatement    STORE_QUERY;
    private final PreparedStatement    DELETE_QUERY;
    private final PreparedStatement    LOAD_ALL_QUERY;
    private final PreparedStatement    DELETE_ALL_QUERY;
    private final Session              session;

    public BaseCassandraMapStore(
            String keyspace,
            String table,
            KeyMapper<K> keyMapper,
            CassandraMapper<V> mapper,
            CassandraConfiguration configuration ) {
        this.keyMapper = keyMapper;
        this.mapper = mapper;
        Cluster.Builder b = Cluster.builder();
        configuration.getCassandraSeedNodes().forEach( ( node ) -> b.addContactPoint( node ) );
        cluster = b.build();

        Metadata metadata = cluster.getMetadata();
        logger.info( "Connected to cluster: {}", metadata.getClusterName() );
        for ( Host host : metadata.getAllHosts() ) {
            logger.info(
                    "Datacenter: {}; Host: {}; Rack: {}\n",
                    host.getDatacenter(),
                    host.getAddress(),
                    host.getRack() );
        }

        session = cluster.newSession();
        session.execute( String.format( KEYSPACE_QUERY, keyspace ) );
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
        ResultSet s;
        try {
            s = session.execute( STORE_QUERY.bind( keyMapper.fromKey( key ), mapper.asString( value ) ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to store key {} : value {} ", key, value );
        }
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        map.forEach( ( K k, V v ) -> {
            store( k, v );
        } );
    }

    @Override
    public void delete( K key ) {
        ResultSet s;
        try {
            s = session.execute( DELETE_QUERY.bind( keyMapper.fromKey( key ) ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to delete key {} : value {} ", key );
        }
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        ResultSet s;
        s = session.execute( DELETE_ALL_QUERY.bind( keys ) );
    }

}
