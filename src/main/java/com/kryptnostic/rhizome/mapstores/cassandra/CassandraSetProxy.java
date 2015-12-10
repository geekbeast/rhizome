package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.kryptnostic.rhizome.mappers.SetProxy;

public class CassandraSetProxy<T> implements SetProxy<T> {
    public static final String SET_ID_FIELD = "setId";
    private final Session session;
    private final String  table;
    private final UUID setId;

    public CassandraSetProxy( Cluster cluster, String keyspace, String table, UUID setId ) {
        this.session = cluster.connect( keyspace );
        this.table = table;
        this.setId = setId;
    }

    @Override
    public int size() {
        session.execute( QueryBuilder.select().all().from( table ).where( QueryBuilder.eq(  SET_ID_FIELD, setId ) );
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains( Object o ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add( T e ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove( Object o ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

}
