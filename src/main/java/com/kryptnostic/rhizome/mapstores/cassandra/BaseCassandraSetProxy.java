package com.kryptnostic.rhizome.mapstores.cassandra;

import java.util.Collection;
import java.util.Iterator;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.openlattice.rhizome.hazelcast.SetProxy;

public abstract class BaseCassandraSetProxy<K, T> implements SetProxy<K, T> {

    protected static final String UNSTABLE_API_EXCEPTION = "Unstable API, this call not supported yet, ping Drew Bailey, drew@kryptnostic.com";

    protected final Session session;
    protected final String  keyspace;
    protected final String  table;

    protected final Statement     GET_STATEMENT;
    protected final Where         SIZE_STATEMENT;

    public static class ProxyKey {

        private final String keyspace;
        private final String table;

        public ProxyKey( String keyspace, String table ) {
            this.keyspace = keyspace;
            this.table = table;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( keyspace == null ) ? 0 : keyspace.hashCode() );
            result = prime * result + ( ( table == null ) ? 0 : table.hashCode() );
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass() != obj.getClass() ) return false;
            ProxyKey other = (ProxyKey) obj;
            if ( keyspace == null ) {
                if ( other.keyspace != null ) return false;
            } else if ( !keyspace.equals( other.keyspace ) ) return false;
            if ( table == null ) {
                if ( other.table != null ) return false;
            } else if ( !table.equals( other.table ) ) return false;
            return true;
        }
    }

    public BaseCassandraSetProxy(
            Session session,
            String keyspace,
            String table,
            Statement getPageStatement,
            Select.Where sizeStatement
            ) {
        this.session = session;
        this.keyspace = keyspace;
        this.table = table;
        GET_STATEMENT = getPageStatement;
        SIZE_STATEMENT = sizeStatement;
    }

    @Override
    public int size() {
        ResultSet execute = session.execute( SIZE_STATEMENT );
        return getCountResult( execute );
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    static int getCountResult( ResultSet resultSet ) {
        Row one = resultSet.one();
        long num = one.getLong( CassandraQueryConstants.COUNT_RESULT_COLUMN_NAME );
        return Math.toIntExact( num );
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException( UNSTABLE_API_EXCEPTION );
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        throw new UnsupportedOperationException( UNSTABLE_API_EXCEPTION );
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        throw new UnsupportedOperationException( UNSTABLE_API_EXCEPTION );
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        throw new UnsupportedOperationException( UNSTABLE_API_EXCEPTION );
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException( UNSTABLE_API_EXCEPTION );
    }

    protected abstract T mapRowToValue( Row row );

    @Override
    public Iterator<T> iterator() {
        return new PagingCassandraIterator<>( session, GET_STATEMENT, ( Row row ) -> mapRowToValue( row ) );
    }

    /**
     * @return the table
     */
    public String getTable() {
        return table;
    }

    /**
     * @return the keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

}
