package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Iterator;
import java.util.function.Function;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public class PagingCassandraIterator<T> implements Iterator<T> {

    private final Iterator<Row> internalIterator;
    private final Function<Row, T> rowMappingFunction;

    public PagingCassandraIterator( Session session, Statement getPageStatement, Function<Row, T> rowMappingFunction ) {
        this.rowMappingFunction = rowMappingFunction;
        ResultSet currentResults = session.execute( getPageStatement );
        internalIterator = currentResults.iterator();
    }

    @Override
    public boolean hasNext() {
        return internalIterator.hasNext();
    }

    @Override
    public T next() {
        Row next = internalIterator.next();
        return rowMappingFunction.apply( next );
    }
}
