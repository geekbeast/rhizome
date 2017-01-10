package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

public abstract class AbstractStructuredCassandraMapstore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    private static final Logger         logger = LoggerFactory
            .getLogger( AbstractStructuredCassandraMapstore.class );
    private final Session               session;
    private final String                mapName;
    private final CassandraTableBuilder tableBuilder;
    private final PreparedStatement     allKeysQuery;
    private final PreparedStatement     loadQuery;
    private final PreparedStatement     storeQuery;
    private final PreparedStatement     deleteQuery;

    public AbstractStructuredCassandraMapstore(
            String mapName,
            Session session,
            CassandraTableBuilder tableBuilder ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( mapName ), "Map name cannot be blank" );
        this.mapName = mapName;
        this.session = Preconditions.checkNotNull( session, "Cassandra session cannot be null" );
        this.tableBuilder = Preconditions.checkNotNull( tableBuilder, "Table builder is required" );
        createCassandraSchemaIfNotExist( session, tableBuilder );
        allKeysQuery = prepareLoadAllKeysQuery();
        loadQuery = prepareLoadQuery();
        storeQuery = prepareStoreQuery();
        deleteQuery = prepareDeleteQuery();
    }

    private void createCassandraSchemaIfNotExist(
            Session session,
            CassandraTableBuilder tableBuilder ) {
        session.execute(
                String.format(
                        "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION={ 'class' : 'SimpleStrategy', 'replication_factor' : %d } AND DURABLE_WRITES=true",
                        tableBuilder.getKeyspace().or( "sparks" ),
                        tableBuilder.getReplicationFactor() ) );
        session.execute( tableBuilder.buildCreateTableQuery() );
    }

    @Override
    public V load( K key ) {
        return safeTransform( asyncLoad( key ) );
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        return keys.stream().map( k -> Pair.of( k, asyncLoad( k ) ) )
                .map( p -> Pair.of( p.getLeft(), safeTransform( p.getRight() ) ) )
                .filter( p -> p.getRight() != null )
                .collect( Collectors.toMap( p -> p.getLeft(), p -> p.getRight() ) );
    }

    @Override
    public Iterable<K> loadAllKeys() {
        /*
         * One limitation of this is that if key stream isn't unique then values may get loaded into the map multiple times.
         */
        ResultSet rs = session.execute( getLoadAllKeysQuery().bind() );
        return Iterables.transform( rs, this::mapKey );
    }

    @Override
    public void store( K key, V value ) {
        asyncStore( key, value ).getUninterruptibly();
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        map.entrySet().parallelStream().map( this::asyncStoreEntry ).forEach( ResultSetFuture::getUninterruptibly );
    }

    @Override
    public void delete( K key ) {
        asyncDelete( key ).getUninterruptibly();
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        keys.parallelStream().map( this::asyncDelete ).forEach( ResultSetFuture::getUninterruptibly );
    }

    protected ResultSetFuture asyncLoad( K key ) {
        try {
            return session.executeAsync( bind( key, getLoadQuery().bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to load key {}", key, e );
            return null;
        }
    }

    protected V safeTransform( ResultSetFuture rsf ) {
        ResultSet rs = rsf.getUninterruptibly();
        return rs == null ? null : mapValue( rs );
    }

    protected ResultSetFuture asyncStore( K key, V value ) {
        try {
            return session.executeAsync( bind( key, value, getStoreQuery().bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to store key {}", key, e );
            return null;
        }
    }

    protected ResultSetFuture asyncDelete( K key ) {
        try {
            return session.executeAsync( bind( key, getDeleteQuery().bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to delete key {}", key, e );
            return null;
        }
    }

    private ResultSetFuture asyncStoreEntry( Entry<K, V> entry ) {
        return asyncStore( entry.getKey(), entry.getValue() );
    }

    protected RegularStatement loadAllKeyQuery() {
        return tableBuilder.buildLoadAllPrimaryKeysQuery();
    }

    protected PreparedStatement prepareLoadAllKeysQuery() {
        return session.prepare( loadAllKeyQuery() );
    }

    protected RegularStatement loadQuery() {
        return tableBuilder.buildLoadQuery();
    }

    protected PreparedStatement prepareLoadQuery() {
        return session.prepare( loadQuery() );
    }

    protected RegularStatement storeQuery() {
        return tableBuilder.buildStoreQuery();
    }

    protected PreparedStatement prepareStoreQuery() {
        return session.prepare( storeQuery() );
    }

    protected RegularStatement deleteQuery() {
        return tableBuilder.buildDeleteQuery();
    }

    protected PreparedStatement prepareDeleteQuery() {
        return session.prepare( deleteQuery() );
    }

    protected PreparedStatement getLoadAllKeysQuery() {
        return allKeysQuery;
    }

    protected PreparedStatement getLoadQuery() {
        return loadQuery;
    }

    protected PreparedStatement getStoreQuery() {
        return storeQuery;
    }

    protected PreparedStatement getDeleteQuery() {
        return deleteQuery;
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return new MapStoreConfig().setImplementation( this ).setEnabled( true )
                .setWriteDelaySeconds( 0 );
    }

    @Override
    public MapConfig getMapConfig() {
        return new MapConfig( mapName ).setBackupCount( this.tableBuilder.getReplicationFactor() )
                .setMapStoreConfig( getMapStoreConfig() );
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Override
    public String getTable() {
        return tableBuilder.getName();
    }

    protected abstract BoundStatement bind( K key, BoundStatement bs );

    protected abstract BoundStatement bind( K key, V value, BoundStatement bs );

    protected abstract K mapKey( Row rs );

    protected abstract V mapValue( ResultSet rs );

}
