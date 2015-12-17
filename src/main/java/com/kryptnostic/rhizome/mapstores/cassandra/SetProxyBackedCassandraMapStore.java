package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.geekbeast.rhizome.configuration.cassandra.CassandraConfiguration;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;

public class SetProxyBackedCassandraMapStore<K, V extends SetProxy<K, ST>, ST> extends BaseCassandraMapStore<K, V> {

    private final ValueMapper<ST>   innerTypeValueMapper;
    private final PreparedStatement LOAD_ALL_KEYS;

    public SetProxyBackedCassandraMapStore(
            String tableName,
            String mapName,
            KeyMapper<K> keyMapper,
            ValueMapper<ST> valueMapper,
            CassandraConfiguration config,
            Session session ) {
        super( tableName, mapName, keyMapper, new SetProxyAwareValueMapper<V, ST>( valueMapper ), config, session );
        this.innerTypeValueMapper = valueMapper;

        this.LOAD_ALL_KEYS = session.prepare(
                QueryBuilder
                        .select( SetProxy.KEY_COLUMN_NAME )
                        .distinct()
                        .from( keyspace, table ) );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#load(java.lang.Object)
     */
    @Override
    public V load( K key ) {
        return (V) new CassandraSetProxy<K, ST>( session, mapName, table, key, keyMapper, innerTypeValueMapper );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#loadAll(java.util.Collection)
     */
    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> results = Maps.newHashMapWithExpectedSize( keys.size() );
        CassandraSetProxy<K, ST> value;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#storeAll(java.util.Map)
     */
    @Override
    public void storeAll( Map<K, V> map ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#delete(java.lang.Object)
     */
    @Override
    public void delete( K key ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    /*
     * (non-Javadoc)
     * @see com.kryptnostic.rhizome.mapstores.cassandra.BaseCassandraMapStore#deleteAll(java.util.Collection)
     */
    @Override
    public void deleteAll( Collection<K> keys ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public K generateTestKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public V generateTestValue() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException( "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

}
