package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;

/**
 * Each partition key should correspond to a <b>unique</b> value in a (partition key)-value store. This <b>does not</b>
 * mean there is a unique row for each partition key; rather, all rows for the partition key should have the same value.
 * If row uniqueness for each partition key needs to be enforced, override the {@link #store(Object, Object)} function
 * with {@link AbstractStructuredCassandraPartitionKeyValueStore#replace(Object, Object)}
 * 
 * @author Ho Chung Siu
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractStructuredCassandraPartitionKeyValueStore<K, V>
        extends AbstractStructuredCassandraMapstoreBase<K, V> {
    private static final Logger logger = LoggerFactory
            .getLogger( AbstractStructuredCassandraPartitionKeyValueStore.class );

    public AbstractStructuredCassandraPartitionKeyValueStore(
            String mapName,
            Session session,
            CassandraTableBuilder tableBuilder ) {
        super( mapName, session, tableBuilder );
    }

    @Override
    protected RegularStatement loadQuery() {
        return tableBuilder.buildLoadByPartitionKeyQuery();
    }

    @Override
    protected RegularStatement deleteQuery() {
        return tableBuilder.buildDeleteByPartitionKeyQuery();
    }

    @Override protected RegularStatement loadAllKeysQuery() {
        return super.loadAllKeysQuery();
    }

    /**
     * We assume that the (partition key)-value store has a unique value for each partition key. This has to be true for
     * a (partition key)-value store to make sense.
     */
    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        return keys.stream().distinct().map( k -> Pair.of( k, asyncLoad( k ) ) )
                .map( p -> Pair.of( p.getLeft(), safeTransform( p.getRight() ) ) )
                .filter( p -> p.getRight() != null )
                .collect( Collectors.toMap( p -> p.getLeft(), p -> p.getRight() ) );
    }

    /**
     * Used to replace the value in a (partition key)-value store. A separate delete is needed, because key here is only
     * the partition key, not the full primary key.
     */
    protected void replace( K key, V value ) {
        delete( key );
        asyncStore( key, value ).getUninterruptibly();
    }
}
