package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Iterator;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;

public class CassandraSetProxy<K, T> implements SetProxy<K, T> {
    public static final String SET_ID_FIELD = "setId";
    private final Session      session;
    private final String       table;
    private final K            setId;
    private final Class<T>     clazz;

    public CassandraSetProxy( Session session, String keyspace, String table, K setId, Class<T> concreteClass ) {
        // use cluster.newSession() here to avoid having to connect to cassandra on object creation
        this.session = session;
        // fully qualify table names so that we can get away with
        // passing around one session throughout the application
        this.table = keyspace.concat( "." ).concat( table );
        this.setId = setId;
        this.clazz = concreteClass;
    }

    @Override
    public int size() {
        Where countQuery = QueryBuilder.select().countAll().from( table ).where( QueryBuilder.eq( SET_ID_FIELD, setId ) );
        ResultSet execute = session.execute( countQuery );
        return execute.all().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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

    @Override
    public Class<T> getType() {
        return clazz;
    }

}
