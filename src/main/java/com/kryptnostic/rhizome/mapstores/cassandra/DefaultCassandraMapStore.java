package com.kryptnostic.rhizome.mapstores.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

import jersey.repackaged.com.google.common.collect.Iterables;

public class DefaultCassandraMapStore<K, V> extends BaseCassandraMapStore<K, V> {
    private static final Logger     logger         = LoggerFactory.getLogger( DefaultCassandraMapStore.class );

    private static final String     KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String     TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (id text PRIMARY KEY, data blob);";

    private final PreparedStatement LOAD_QUERY;
    private final PreparedStatement STORE_QUERY;
    private final PreparedStatement DELETE_QUERY;
    private final PreparedStatement LOAD_ALL_QUERY;
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

        LOAD_ALL_QUERY = session.prepare( QueryBuilder
                .select( DEFAULT_KEY_COLUMN_NAME ).from( keyspace, table ) );

        DELETE_ALL_QUERY = session.prepare( QueryBuilder
                .delete().from( keyspace, table )
                .where( QueryBuilder.in( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

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
    public Set<K> loadAllKeys() {
        ResultSet s;
        // TODO: make this return an Iterable<K> that lazily loads values (one page at a time)
        s = session.execute( LOAD_ALL_QUERY.bind() );
        return Sets.newHashSet( Iterables.transform( s.all(), ( Row r ) -> {
            return keyMapper.toKey( r.getString( "id" ) );
        } ) );
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

}
