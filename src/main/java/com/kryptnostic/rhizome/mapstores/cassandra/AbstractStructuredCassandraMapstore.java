package com.kryptnostic.rhizome.mapstores.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;

public abstract class AbstractStructuredCassandraMapstore<K, V> extends AbstractStructuredCassandraMapstoreBase<K, V> {
    private static final Logger         logger = LoggerFactory
            .getLogger( AbstractStructuredCassandraMapstore.class );

    public AbstractStructuredCassandraMapstore(
            String mapName,
            Session session,
            CassandraTableBuilder tableBuilder ) {
        super( mapName, session, tableBuilder );
    }

    @Override
    protected RegularStatement loadQuery(){
        return tableBuilder.buildLoadQuery();
    }

    @Override
    protected RegularStatement deleteQuery(){
        return tableBuilder.buildDeleteByPrimaryKeyQuery();
    }
}
