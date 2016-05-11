package com.kryptnostic.rhizome.mapstores.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class DefaultCassandraMapStore<K, V> extends BaseCassandraMapStore<K, V> {
    private static final Logger     logger         = LoggerFactory.getLogger( DefaultCassandraMapStore.class );

    private static final String     KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String     TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (id text PRIMARY KEY, data blob);";

    private final PreparedStatement LOAD_QUERY;
    private final PreparedStatement STORE_QUERY;
    private final PreparedStatement DELETE_QUERY;
    private final Select            LOAD_ALL_QUERY;
    private final PreparedStatement DELETE_ALL_QUERY;

    public DefaultCassandraMapStore(
            String table,
            String mapName,
            SelfRegisteringKeyMapper<K> keyMapper,
            SelfRegisteringValueMapper<V> mapper,
            CassandraConfiguration config,
            Session globalSession ) {
        super( table, mapName, keyMapper, mapper, config, globalSession );

        session.execute( String.format( KEYSPACE_QUERY, keyspace, replicationFactor ) );
        session.execute( String.format( TABLE_QUERY, keyspace, table ) );

        LOAD_QUERY = session.prepare( QueryBuilder
                .select( DEFAULT_VALUE_COLUMN_NAME ).from( keyspace, table )
                .where( QueryBuilder.eq( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        STORE_QUERY = session.prepare( QueryBuilder
                .insertInto( keyspace, table )
                .value( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() )
                .value( DEFAULT_VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) );

        DELETE_QUERY = session.prepare( QueryBuilder
                .delete().from( keyspace, table )
                .where( QueryBuilder.eq( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        LOAD_ALL_QUERY = QueryBuilder
                .select( DEFAULT_KEY_COLUMN_NAME ).from( keyspace, table );

        DELETE_ALL_QUERY = session.prepare( QueryBuilder
                .delete().from( keyspace, table )
                .where( QueryBuilder.in( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

    }

    @Override
    public V load( K key ) {
        ResultSet s;
        s = session.execute( LOAD_QUERY.bind( keyMapper.fromKey( key ) ) );
        Row result = s.one();
        if ( result == null ) {
            return null;
        }
        return mapToValue( result );
    }

    @Override
    public Iterable<K> loadAllKeys() {
        return PagingCassandraIterator.asIterable( session, LOAD_ALL_QUERY, this::mapToKey );
    }

    @Override
    public K mapToKey( Row row ) {
        return keyMapper.toKey( row.getString( "id" ) );
    }

    @Override
    public void store( K key, V value ) {
        try {
            session.execute(
                    STORE_QUERY.bind( keyMapper.fromKey( key ), ByteBuffer.wrap( valueMapper.toBytes( value ) ) ) );
        } catch ( MappingException e ) {
            logger.error( "Unable to store key {} : value {} ", key, value, e );
        }
    }

    @Override
    public void delete( K key ) {
        session.execute( DELETE_QUERY.bind( keyMapper.fromKey( key ) ) );
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        List<String> mappedKeys = new ArrayList<>( keys.size() );
        for ( K key : keys ) {
            mappedKeys.add( keyMapper.fromKey( key ) );
        }
        session.execute( DELETE_ALL_QUERY.bind( mappedKeys ) );
    }

    @Override
    protected ResultSetFuture asyncLoad( K key ) {
        return session.executeAsync( LOAD_QUERY.bind( keyMapper.fromKey( key ) ) );
    }

    @Override
    protected V mapToValue( Row row ) {
        ByteBuffer bytes = row.getBytes( "data" );
        try {
            return valueMapper.fromBytes( bytes.array() );
        } catch ( MappingException e ) {
            logger.error( "cant wait to kill these valuemappers" );
        }
        return null;
    }

}
