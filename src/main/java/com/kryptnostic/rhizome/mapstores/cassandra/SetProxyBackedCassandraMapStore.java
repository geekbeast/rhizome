package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraSetProxy.ProxyKey;

public class SetProxyBackedCassandraMapStore<K, V extends Set<T>, T> extends BaseCassandraMapStore<K, V> {

    private static final String                     KEYSPACE_QUERY         = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String                     TABLE_QUERY            = "CREATE TABLE IF NOT EXISTS %s.%s (%s text, %s %s, PRIMARY KEY( %s, %s ) );";

    private final SelfRegisteringValueMapper<T>     innerTypeValueMapper;
    private final PreparedStatement                 DELETE_KEY;
    private final Class<T>                          innerType;
    private final PreparedStatement                 DELETE_ALL_QUERY;
    private K                                       testKey;
    private V                                       testValue;
    private final Select                            LOAD_ALL_KEYS_PAGED;

    static final Cache<ProxyKey, PreparedStatement> SP_CONTAINS_STATEMENTS = CacheBuilder.newBuilder()
                                                                                   .build();
    static final Cache<ProxyKey, PreparedStatement> SP_ADD_STATEMENTS      = CacheBuilder.newBuilder()
                                                                                   .build();
    static final Cache<ProxyKey, PreparedStatement> SP_DELETE_STATEMENTS   = CacheBuilder.newBuilder()
                                                                                   .build();

    public SetProxyBackedCassandraMapStore(
            String tableName,
            String mapName,
            SelfRegisteringKeyMapper<K> keyMapper,
            SelfRegisteringValueMapper<T> valueMapper,
            CassandraConfiguration config,
            Session session,
            Class<T> innerType,
            K testKey,
            V testValue ) {
        super( tableName, mapName, keyMapper, new SetProxyAwareValueMapper<V, T>( valueMapper ), config, session );
        this.innerTypeValueMapper = valueMapper;
        this.innerType = innerType;
        this.testKey = testKey;
        this.testValue = testValue;

        this.SUPPORTS_ASYNC_LOADS = false;

        // create keyspace
        session.execute( String.format( KEYSPACE_QUERY, keyspace, replicationFactor ) );

        String cassValType = DataType.blob().asFunctionParameterString();
        // create table
        session.execute( String.format( TABLE_QUERY,
                keyspace,
                table,
                SetProxy.KEY_COLUMN_NAME,
                SetProxy.VALUE_COLUMN_NAME,
                cassValType,
                SetProxy.KEY_COLUMN_NAME,
                SetProxy.VALUE_COLUMN_NAME ) );

        this.LOAD_ALL_KEYS_PAGED = QueryBuilder
                .select( SetProxy.KEY_COLUMN_NAME )
                .from( keyspace, table );

        this.DELETE_KEY = session.prepare(
                QueryBuilder
                        .delete()
                        .from( keyspace, table )
                        .where( QueryBuilder.eq( SetProxy.KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        this.DELETE_ALL_QUERY = session.prepare(
                QueryBuilder.delete().from( keyspace, table )
                        .where( QueryBuilder.in( SetProxy.KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        DefaultCassandraSetProxy.ProxyKey key = new DefaultCassandraSetProxy.ProxyKey( keyspace, table );

        try {
            SP_ADD_STATEMENTS.get( key, () -> session.prepare(
                    QueryBuilder
                            .insertInto( keyspace, table )
                            .value( SetProxy.KEY_COLUMN_NAME, QueryBuilder.bindMarker() )
                            .value( SetProxy.VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );
            SP_DELETE_STATEMENTS.get( key, () -> session.prepare(
                    QueryBuilder
                            .delete()
                            .from( keyspace, table )
                            .where( QueryBuilder.eq( SetProxy.KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) )
                            .and( QueryBuilder.eq( SetProxy.VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) ) ) );
            // Read-only calls
            SP_CONTAINS_STATEMENTS.get( key, () -> session.prepare(
                    QueryBuilder.select().countAll().from( keyspace, table )
                            .where( QueryBuilder.eq( SetProxy.KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) )
                            .and( QueryBuilder.eq( SetProxy.VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) ) ) );
        } catch ( ExecutionException e ) {
            throw Throwables.propagate( e );
        }
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#load(java.lang.Object)
     */
    @Nonnull
    @Override
    public V load( K key ) {
        // consider having this run a check to see if there are no entries and return null instead if so
        // --this way containsKey on this IMap will actually be worth doing
        return (V) new DefaultCassandraSetProxy<K, T>(
                session,
                keyspace,
                table,
                keyMapper.fromKey( key ),
                innerType,
                innerTypeValueMapper );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#loadAll(java.util.Collection)
     */
    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> results = Maps.newHashMapWithExpectedSize( keys.size() );
        DefaultCassandraSetProxy<K, T> value;
        for ( K key : keys ) {
            value = new DefaultCassandraSetProxy<>(
                    session,
                    keyspace,
                    table,
                    keyMapper.fromKey( key ),
                    innerType,
                    innerTypeValueMapper );
            results.put( key, (V) value );
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#loadAllKeys()
     */
    @Override
    public Iterable<K> loadAllKeys() {
        Set<K> results = Sets.newHashSet();
        for ( Row row : session.execute( LOAD_ALL_KEYS_PAGED ).all() ) {
            results.add( mapToKey( row ) );
        }
        return results;
    }

    @Override
    public K mapToKey( Row row ) {
        return keyMapper.toKey( row.getString( SetProxy.KEY_COLUMN_NAME ) );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#store(java.lang.Object, java.lang.Object)
     */
    @Override
    public void store( K key, V value ) {
        SetProxy<K, T> proxy = new DefaultCassandraSetProxy<K, T>(
                session,
                keyspace,
                table,
                keyMapper.fromKey( key ),
                innerType,
                innerTypeValueMapper );
        proxy.addAll( value );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#storeAll(java.util.Map)
     */
    @Override
    public void storeAll( Map<K, V> map ) {
        map.forEach( ( K key, V setValue ) -> {
            SetProxy<K, T> proxy = new DefaultCassandraSetProxy<K, T>(
                    session,
                    keyspace,
                    table,
                    keyMapper.fromKey( key ),
                    innerType,
                    innerTypeValueMapper );
            proxy.addAll( setValue );
        } );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#delete(java.lang.Object)
     */
    @Override
    public void delete( K key ) {
        String mapped = keyMapper.fromKey( key );
        session.execute( DELETE_KEY.bind( mapped ) );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#deleteAll(java.util.Collection)
     */
    @Override
    public void deleteAll( Collection<K> keys ) {
        List<String> mappedKeys = new ArrayList<>( keys.size() );
        for ( K key : keys ) {
            mappedKeys.add( keyMapper.fromKey( key ) );
        }
        session.execute( DELETE_ALL_QUERY.bind( mappedKeys ) );
    }

    @Override
    public K generateTestKey() {
        return testKey;
    }

    @Override
    public V generateTestValue() {
        return testValue;
    }

    @Override
    protected ResultSetFuture asyncLoad( K key ) {
        throw new UnsupportedOperationException( "SetProxy does not support async loads" );
    }

    @Override
    protected V mapToValue( Row row ) {
        throw new UnsupportedOperationException( "SetProxy does not support mapping values" );
    }

}
