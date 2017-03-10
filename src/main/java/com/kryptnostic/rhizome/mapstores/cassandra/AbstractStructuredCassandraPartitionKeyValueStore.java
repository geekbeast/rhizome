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

public abstract class AbstractStructuredCassandraPartitionKeyValueStore<K, V> extends AbstractStructuredCassandraMapstoreBase<K, V> {
    private static final Logger         logger = LoggerFactory
            .getLogger( AbstractStructuredCassandraPartitionKeyValueStore.class );

    public AbstractStructuredCassandraPartitionKeyValueStore(
            String mapName,
            Session session,
            CassandraTableBuilder tableBuilder ) {
        super( mapName, session, tableBuilder );
    }

    @Override
    protected RegularStatement loadQuery(){
        return tableBuilder.buildLoadByPartitionKeyQuery();
    }
    
    @Override
    protected RegularStatement deleteQuery(){
        return tableBuilder.buildDeleteByPartitionKeyQuery();
    }
  
    /**
     * We assume that the (partition key)-value store has a unique value for each partition key. This has to be true for a (partition key)-value store to make sense.
     */
    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        return keys.stream().map( k -> Pair.of( k, asyncLoad( k ) ) )
                .map( p -> Pair.of( p.getLeft(), safeTransform( p.getRight() ) ) )
                .filter( p -> p.getRight() != null )
                .collect( Collectors.toMap( p -> p.getLeft(), p -> p.getRight(), (left, right) -> right ) );
    }
}
