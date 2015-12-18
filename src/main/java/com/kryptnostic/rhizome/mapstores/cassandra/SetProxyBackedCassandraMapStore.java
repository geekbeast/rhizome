package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete.Where;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;

public class SetProxyBackedCassandraMapStore<K, V extends Set<T>, T> extends BaseCassandraMapStore<K, V> {

    private final ValueMapper<T>   innerTypeValueMapper;
    private final PreparedStatement LOAD_ALL_KEYS;
    private final PreparedStatement DELETE_KEY;

    public SetProxyBackedCassandraMapStore(
            String tableName,
            String mapName,
            KeyMapper<K> keyMapper,
            ValueMapper<T> valueMapper,
            CassandraConfiguration config,
            Session session ) {
        super( tableName, mapName, keyMapper, new SetProxyAwareValueMapper<V, T>( valueMapper ), config, session );
        this.innerTypeValueMapper = valueMapper;

        this.LOAD_ALL_KEYS = session.prepare(
                QueryBuilder
                        .select( SetProxy.KEY_COLUMN_NAME )
                        .distinct()
                        .from( keyspace, table ) );

        this.DELETE_KEY = session.prepare(
                QueryBuilder
                        .delete( SetProxy.KEY_COLUMN_NAME, SetProxy.VALUE_COLUMN_NAME )
                        .from( keyspace, table )
                        .where( QueryBuilder.eq( SetProxy.KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#load(java.lang.Object)
     */
    @Nonnull
    @Override
    public V load( K key ) {
        return (V) new CassandraSetProxy<K, T>( session, keyspace, table, key, keyMapper, innerTypeValueMapper );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#loadAll(java.util.Collection)
     */
    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> results = Maps.newHashMapWithExpectedSize( keys.size() );
        CassandraSetProxy<K, T> value;
        for ( K key : keys ) {
            value = new CassandraSetProxy<>( session, mapName, table, key, keyMapper, innerTypeValueMapper );
            results.put( key, (V) value );
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#loadAllKeys()
     */
    @Override
    public Set<K> loadAllKeys() {
        List<Row> execute = session.execute( LOAD_ALL_KEYS.bind() ).all();
        Set<K> set = Sets.newHashSetWithExpectedSize( execute.size() );
        for ( Row row : execute ) {
            K key = keyMapper.toKey( row.getString( CassandraQueryConstants.VALUE_RESULT_COLUMN_NAME ) );
            set.add( key );
        }
        return set;
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#store(java.lang.Object, java.lang.Object)
     */
    @Override
    public void store( K key, V value ) {
        SetProxy<K, T> proxy = new CassandraSetProxy<K, T>( session, keyspace, table, key, keyMapper, innerTypeValueMapper );
        proxy.addAll( value );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#storeAll(java.util.Map)
     */
    @Override
    public void storeAll( Map<K, V> map ) {
        map.forEach( ( K key, V setValue ) -> {
            SetProxy<K, T> proxy = new CassandraSetProxy<K, T>( session, keyspace, table, key, keyMapper, innerTypeValueMapper );
            proxy.addAll( setValue );
        } );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#delete(java.lang.Object)
     */
    @Override
    public void delete( K key ) {
        String keyString = keyMapper.toString();
        session.execute( DELETE_KEY.bind( keyString ) );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#deleteAll(java.util.Collection)
     */
    @Override
    public void deleteAll( Collection<K> keys ) {
        Where deleteAll = QueryBuilder.delete( SetProxy.KEY_COLUMN_NAME, SetProxy.VALUE_COLUMN_NAME )
                .from( keyspace, table )
                .where( QueryBuilder.in( SetProxy.KEY_COLUMN_NAME, keys ) );
        session.execute( deleteAll );
    }

    @Override
    public K generateTestKey() {
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public V generateTestValue() throws Exception {
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

}
