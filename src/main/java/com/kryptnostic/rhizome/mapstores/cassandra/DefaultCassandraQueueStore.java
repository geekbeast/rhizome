package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.QueueStoreConfig;
import com.kryptnostic.rhizome.configuration.cassandra.CassandraConfiguration;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringQueueStore;

public class DefaultCassandraQueueStore<T> implements SelfRegisteringQueueStore<T> {
    private static final Logger   logger         = LoggerFactory.getLogger( DefaultCassandraQueueStore.class );

    private static final String DEFAULT_KEY_COLUMN_NAME   = "id";
    private static final String DEFAULT_VALUE_COLUMN_NAME = "data";
    private static final String   KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':%d};";
    private static final String   TABLE_QUERY    = "CREATE TABLE IF NOT EXISTS %s.%s (id bigint PRIMARY KEY, data %s);";

    protected final Session       session;
    protected final String        keyspace;
    protected final int           replicationFactor;
    private final String          mapName;
    private final String          table;

    private PreparedStatement   LOAD_QUERY;

    private PreparedStatement   STORE_QUERY;

    private PreparedStatement   DELETE_QUERY;

    private PreparedStatement   LOAD_ALL_QUERY;

    private PreparedStatement   DELETE_ALL_QUERY;

    public DefaultCassandraQueueStore(
            String tableName,
            String mapName,
            CassandraConfiguration config,
            Session globalSession,
            Class<T> theClass
            ) {
        this.table = tableName;
        this.mapName = mapName;
        this.session = globalSession;
        this.keyspace = config.getKeyspace();
        this.replicationFactor = config.getReplicationFactor();

        String cassandraValueType = CassandraQueryConstants.cassandraValueType( theClass );
        session.execute( String.format( KEYSPACE_QUERY, keyspace, replicationFactor ) );
        session.execute( String.format( TABLE_QUERY, keyspace, table, cassandraValueType ) );

        LOAD_QUERY = session.prepare( QueryBuilder.select( DEFAULT_VALUE_COLUMN_NAME )
                .from( keyspace, table )
                .where( QueryBuilder.eq( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        STORE_QUERY = session.prepare( QueryBuilder.insertInto( keyspace, table )
                .value( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() )
                .value( DEFAULT_VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) );

        DELETE_QUERY = session.prepare( QueryBuilder.delete()
                .from( keyspace, table )
                .where( QueryBuilder.eq( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        LOAD_ALL_QUERY = session.prepare( QueryBuilder.select( DEFAULT_KEY_COLUMN_NAME )
                .from( keyspace, table ) );

        DELETE_ALL_QUERY = session.prepare( QueryBuilder.delete()
                .from( keyspace, table )
                .where( QueryBuilder.in( DEFAULT_KEY_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );
    }

    @Override
    public void store( Long key, T value ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public void storeAll( Map<Long, T> map ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public void delete( Long key ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public void deleteAll( Collection<Long> keys ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public T load( Long key ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public Map<Long, T> loadAll( Collection<Long> keys ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public Set<Long> loadAllKeys() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "THIS METHOD HAS NOT BEEN IMPLEMENTED, BLAME Drew Bailey drew@kryptnostic.com" );
    }

    @Override
    public QueueStoreConfig getQueueStoreConfig() {
        return new QueueStoreConfig().setStoreImplementation( this ).setEnabled( true );
    }

    @Override
    public QueueConfig getQueueConfig() {
        return new QueueConfig( mapName ).setBackupCount( this.replicationFactor )
                .setQueueStoreConfig( getQueueStoreConfig() );
    }

}
