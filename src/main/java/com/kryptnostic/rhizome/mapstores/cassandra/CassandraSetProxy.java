package com.kryptnostic.rhizome.mapstores.cassandra;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.google.common.base.Preconditions;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class CassandraSetProxy<K, T> implements SetProxy<K, T> {
    private static final Logger                             logger  = LoggerFactory.getLogger( CassandraSetProxy.class );

    private final Session                                   session;
    final SelfRegisteringValueMapper<T>                     typeMapper;

    final Where                                             GET_PAGE_STATEMENT;
    private final Where                                     SIZE_STATEMENT;

    private static final String                             MAPPING_ERROR          = "Exception while mapping";

    private static final String                             UNSTABLE_API_EXCEPTION = "Unstable API, this call not supported yet, ping Drew Bailey, drew@kryptnostic.com";

    private final PreparedStatement     CONTAINS_STATEMENT;
    private final PreparedStatement     ADD_STATEMENT;
    private final PreparedStatement     DELETE_STATEMENT;

    private final String                                    keyspace;
    private final String                                    table;
    private final String                                    setId;
    private final Class<T>                                  innerClass;

    static class ProxyKey {

        private final String keyspace;
        private final String table;

        ProxyKey( String keyspace, String table ) {
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

    public CassandraSetProxy(
            Session session,
            String keyspace,
            String table,
            String mappedSetId,
            Class<T> innerClass,
            SelfRegisteringValueMapper<T> typeMapper ) {
        // use cluster.newSession() here to avoid having to connect to cassandra on object creation
        this.session = session;
        this.keyspace = keyspace;
        this.table = table;
        this.setId = mappedSetId;
        this.innerClass = innerClass;
        this.typeMapper = typeMapper;

        ProxyKey key = new ProxyKey( keyspace, table );

        ADD_STATEMENT = SetProxyBackedCassandraMapStore.SP_ADD_STATEMENTS.getIfPresent( key );
        DELETE_STATEMENT = SetProxyBackedCassandraMapStore.SP_DELETE_STATEMENTS.getIfPresent( key );
        CONTAINS_STATEMENT = SetProxyBackedCassandraMapStore.SP_CONTAINS_STATEMENTS.getIfPresent( key );
        Preconditions.checkNotNull( ADD_STATEMENT,
                "something terribly terribly wrong happenend to the SetProxy add statement" );
        Preconditions.checkNotNull( DELETE_STATEMENT,
                "something terribly terribly wrong happenend to the SetProxy delete statement" );
        Preconditions.checkNotNull( CONTAINS_STATEMENT,
                "something terribly terribly wrong happenend to the SetProxy contains statement" );

        // Calls with no variables
        this.SIZE_STATEMENT = QueryBuilder.select().countAll()
                .from( keyspace, table )
                .where( QueryBuilder.eq( KEY_COLUMN_NAME, mappedSetId ) );

        this.GET_PAGE_STATEMENT = QueryBuilder
                        .select( VALUE_COLUMN_NAME )
                        .from( keyspace, table )
                .where( QueryBuilder.eq( KEY_COLUMN_NAME, mappedSetId ) );
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

    @Override
    public boolean contains( Object o ) {
        if( innerClass.isAssignableFrom( o.getClass() ) ) {
            return containsValue( innerClass.cast( o ) );
        }
        return false;
    }

    public boolean containsValue( T value ) {
        ResultSet execute;
        try {
            execute = session.execute( CONTAINS_STATEMENT.bind( toBytes( value ) ) );
            int results = getCountResult( execute );
            return results == 1;
        } catch ( MappingException e ) {
            logger.error( MAPPING_ERROR, e );
        }
        return false;
    }

    private static int getCountResult( ResultSet resultSet ) {
        Row one = resultSet.one();
        long num = one.getLong( CassandraQueryConstants.COUNT_RESULT_COLUMN_NAME );
        return Math.toIntExact( num );
    }

    @Override
    public Iterator<T> iterator() {
        return new SetProxyIterator( session );
    }

    private class SetProxyIterator implements Iterator<T> {

        private final Iterator<Row> internalIterator;

        public SetProxyIterator( Session session ) {
            ResultSet currentResultPage = session.execute( GET_PAGE_STATEMENT );
            internalIterator = currentResultPage.iterator();
        }

        @Override
        public boolean hasNext() {
            return internalIterator.hasNext();
        }

        @Override
        public T next() {
            Row next = internalIterator.next();
            return getObjectFromRow( next );
        }

        private T getObjectFromRow( Row row ) {
            ByteBuffer bytes = row.getBytes( VALUE_COLUMN_NAME );
            byte[] array = bytes.array();
            try {
                T fromBytes = typeMapper.fromBytes( array );
                return fromBytes;
            } catch ( MappingException e ) {
                logger.error( MAPPING_ERROR, e );
            }
            return null;
        }

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
    public boolean add( T e ) {
        if ( contains( e ) ) {
            return false;
        }
        try {
            // add to the set as a new row
            session.execute( ADD_STATEMENT.bind( toBytes( e ) ) );
            return true;
        } catch ( MappingException e1 ) {
            logger.error( MAPPING_ERROR, e1 );
        }
        return false;
    }

    @Override
    public boolean remove( Object o ) {
        try {
            session.execute( DELETE_STATEMENT.bind( toBytes( (T) o ) ) );
            return true;
        } catch ( MappingException e ) {
            logger.error( MAPPING_ERROR, e );
        }
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        for (Object x : c) {
            if (!contains(x)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        // TODO: p1: make this efficient
        boolean ret = false;
        for ( T element : c ) {
            boolean modified = add( element );
            if ( modified ) {
                ret = true;
            }
        }
        return ret;
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

    private ByteBuffer toBytes( T value ) throws MappingException {
        return ByteBuffer.wrap( typeMapper.toBytes( value ) );
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }


    /**
     * @return the typeMapper
     */
    public SelfRegisteringValueMapper<T> getTypeMapper() {
        return typeMapper;
    }

    /**
     * @return the keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }


    /**
     * @return the table
     */
    public String getTable() {
        return table;
    }


    public String getSetId() {
        return setId;
    }

    @Override
    public Class<T> getTypeClazz() {
        return innerClass;
    }

}
