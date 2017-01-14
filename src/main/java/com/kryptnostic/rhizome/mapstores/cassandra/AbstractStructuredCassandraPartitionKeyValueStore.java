package com.kryptnostic.rhizome.mapstores.cassandra;

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
}
