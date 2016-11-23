package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.cassandra.BindingFunction;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;
import com.kryptnostic.rhizome.cassandra.KeyBindingFunction;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;

public abstract class AbstractStructuredCassandraMapstore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    private static final Logger         logger = LoggerFactory
            .getLogger( AbstractStructuredCassandraMapstore.class );
    private final Session               session;
    private final String                mapName;
    private final KeyBindingFunction<K> kf;
    private final BindingFunction<K, V> vsf;
    private final Function<Row, K>      krf;
    private final Function<Row, V>      vf;

    private final CassandraTableBuilder tableBuilder;
    private final PreparedStatement     LOAD_ALL_QUERY;
    private final PreparedStatement     LOAD_QUERY;
    private final PreparedStatement     STORE_QUERY;
    private final PreparedStatement     DELETE_QUERY;

    public AbstractStructuredCassandraMapstore(
            String mapName,
            Session session,
            KeyBindingFunction<K> kf,
            BindingFunction<K, V> vsf,
            Function<Row, K> krf,
            Function<Row, V> vf,
            CassandraTableBuilder tableBuilder ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( mapName ), "Map name cannot be blank" );
        this.mapName = mapName;
        this.session = Preconditions.checkNotNull( session, "Cassandra session cannot be null" );
        this.tableBuilder = Preconditions.checkNotNull( tableBuilder, "Table builder is required" );
        this.vsf = Preconditions.checkNotNull( vsf, "Binding function for storage required." );
        this.kf = Preconditions.checkNotNull( kf, "Key binding fuction for reading and deletes required" );
        this.vf = Preconditions.checkNotNull( vf, "Value transforming function required." );
        this.krf = Preconditions.checkNotNull( krf, "Key reading fuction for loading all keys required" );
        createCassandraSchemaIfNotExist( session, tableBuilder );
        LOAD_ALL_QUERY = prepareLoadAllQuery();
        LOAD_QUERY = prepareLoadQuery();
        STORE_QUERY = prepareStoreQuery();
        DELETE_QUERY = prepareDeleteQuery();
    }

    private void createCassandraSchemaIfNotExist(
            Session session,
            CassandraTableBuilder tableBuilder ) {
        session.execute(
                String.format(
                        "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION={ 'class' : 'SimpleStrategy', 'replication_factor' : %d } AND DURABLE_WRITES=true",
                        tableBuilder.getKeyspace().or( "sparks" ),
                        tableBuilder.getReplicationFactor() ) );
        session.execute( tableBuilder.buildQuery() );
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
        Iterable<Row> rs = session.execute( getLoadAllKeysQuery().bind() );
        return Iterables.transform( rs, krf );
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
            return session.executeAsync( kf.bind( key, getLoadQuery().bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to load key {}", key, e );
            return null;
        }
    }

    protected V safeTransform( ResultSetFuture rsf ) {
        Row row = rsf.getUninterruptibly().one();
        return row == null ? null : vf.apply( row );
    }

    protected ResultSetFuture asyncStore( K key, V value ) {
        try {
            return session.executeAsync( vsf.bind( key, value, getStoreQuery().bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to store key {}", key, e );
            return null;
        }
    }

    protected ResultSetFuture asyncDelete( K key ) {
        try {
            return session.executeAsync( kf.bind( key, getDeleteQuery().bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to delete key {}", key, e );
            return null;
        }
    }

    private ResultSetFuture asyncStoreEntry( Entry<K, V> entry ) {
        return asyncStore( entry.getKey(), entry.getValue() );
    }

    protected PreparedStatement prepareLoadAllQuery() {
        return session.prepare( tableBuilder.buildLoadAllQuery() );
    }

    protected PreparedStatement prepareLoadQuery() {
        return session.prepare( tableBuilder.buildLoadQuery() );
    }

    protected PreparedStatement prepareStoreQuery() {
        return session.prepare( tableBuilder.buildStoreQuery() );
    }

    protected PreparedStatement prepareDeleteQuery() {
        return session.prepare( tableBuilder.buildDeleteQuery() );
    }

    protected PreparedStatement getLoadAllKeysQuery() {
        return LOAD_ALL_QUERY;
    }

    protected PreparedStatement getLoadQuery() {
        return LOAD_QUERY;
    }

    protected PreparedStatement getStoreQuery() {
        return STORE_QUERY;
    }

    protected PreparedStatement getDeleteQuery() {
        return DELETE_QUERY;
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

    @Override
    public abstract K generateTestKey();

    @Override
    public abstract V generateTestValue() throws Exception;

}
