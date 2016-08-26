package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Table;
import com.hazelcast.core.MapStore;
import com.kryptnostic.rhizome.mapstores.cassandra.CassandraOptions.ReplicationStrategy;

import jersey.repackaged.com.google.common.base.Preconditions;

public class MapperCassandraMapstore<K extends CassandraKey, V extends K> implements MapStore<K, V> {
    private static final Logger          logger = LoggerFactory.getLogger( MapperCassandraMapstore.class );
    private Mapper<V>                    valueMapper;
    private CassandraMapStoreAccessor<K> accessor;

    public MapperCassandraMapstore(
            MappingManager mappingManager,
            Class<K> keyClass,
            Class<V> valueClass,
            CassandraMapStoreAccessor<K> accessor,
            Consumer<Session> createTableQuery ) {
        Preconditions.checkArgument( keyClass.getAnnotation( Table.class ) != null,
                "Key class is missing @Table annotation." );
        createCassandraSchemaIfNotExist( mappingManager.getSession(), valueClass, createTableQuery );
        valueMapper = mappingManager.mapper( valueClass );
        this.accessor = accessor;

    }

    private void createCassandraSchemaIfNotExist(
            Session session,
            Class<V> valueClass,
            Consumer<Session> createTableQuery ) {
        Table table = Preconditions.checkNotNull( valueClass.getAnnotation( Table.class ),
                "Value class must have @Table annotation." );
        CassandraOptions options = valueClass.getAnnotation( CassandraOptions.class );
        String keyspace = table.keyspace();
        String strategy;
        int replicationFactor;

        if ( options != null ) {
            strategy = options.strategy().name();
            replicationFactor = options.replicationFactor();
        } else {
            strategy = ReplicationStrategy.SIMPLE.name();
            replicationFactor = 2;
        }

        String query = String.format(
                "CREATE %s IF NOT EXISTS WITH REPLICATION = {'class':'%s','replication_factor' : '%s' }",
                keyspace,
                strategy,
                replicationFactor );

        session.execute( query );

        // now create the table if we need to.

        createTableQuery.accept( session );

    }

    @Override
    public V load( K key ) {
        return valueMapper.get( key.asPrimaryKey() );
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        return keys.parallelStream()
                .collect( Collectors.toMap( key -> key, key -> valueMapper.getAsync( key.asPrimaryKey() ) ) )
                .entrySet().parallelStream().collect( Collectors.toMap( e -> e.getKey(), e -> {
                    try {
                        return e.getValue().get();
                    } catch ( InterruptedException | ExecutionException e1 ) {
                        logger.error( "Unable to load key: {}", e.getKey(), e1 );
                        return null;
                    }
                } ) ).entrySet().parallelStream().filter( e -> e.getValue() != null )
                .collect( Collectors.toMap( e -> e.getKey(), e -> e.getValue() ) );
    }

    @Override
    public Iterable<K> loadAllKeys() {
        return accessor.getAllKeys();
    }

    @Override
    public void store( K key, V value ) {
        valueMapper.save( value );
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        map.entrySet().parallelStream()
                .collect( Collectors.toMap( e -> e.getKey(), e -> valueMapper.saveAsync( e.getValue() ) ) ).entrySet()
                .forEach( ef -> {
                    try {
                        ef.getValue().get();
                    } catch ( InterruptedException | ExecutionException e ) {
                        logger.error( "Failed to write key {} during bulk write op.", ef.getKey(), e );
                    }
                } );
    }

    @Override
    public void delete( K key ) {
        valueMapper.delete( key.asPrimaryKey() );
    }

    @Override
    public void deleteAll( Collection<K> keys ) {
        // TODO: Make this suck less
        keys.parallelStream().map( k -> valueMapper.deleteAsync( k.asPrimaryKey() ) ).forEach( k -> {
            try {
                k.get();
            } catch ( InterruptedException | ExecutionException e ) {
                logger.error( "Error during bulk delete.", e );
            }
        } );
    }

}
