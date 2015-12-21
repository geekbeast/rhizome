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
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class CassandraSetProxy<K, T> implements SetProxy<K, T> {
    private static final Logger     logger                   = LoggerFactory.getLogger( CassandraSetProxy.class );

    private final Session           session;
    final ValueMapper<T>            typeMapper;

    private final PreparedStatement CONTAINS_STATEMENT;
    private final PreparedStatement SIZE_STATEMENT;
    private final PreparedStatement ADD_STATEMENT;
    private final PreparedStatement DELETE_STATEMENT;

    final PreparedStatement         GET_PAGE_STATEMENT;

    private static final String     MAPPING_ERROR            = "Exception while mapping";

    private static final String     UNSTABLE_API_EXCEPTION   = "Unstable API, this call not supported yet, ping Drew Bailey, drew@kryptnostic.com";

    public CassandraSetProxy(
            Session session,
            String keyspace,
            String table,
            K setId,
            KeyMapper<K> keyMapper,
            ValueMapper<T> typeMapper ) {
        // use cluster.newSession() here to avoid having to connect to cassandra on object creation
        this.session = session;
        this.typeMapper = typeMapper;
        // fully qualify table names so that we can get away with
        // passing around one session throughout the application
        String mappedSetId = keyMapper.fromKey( setId );

        final Clause setIsThisSet = QueryBuilder.eq( KEY_COLUMN_NAME, mappedSetId );
        final Select countInTable = QueryBuilder.select().countAll().from( keyspace, table );
        final Clause whereContainsValue = QueryBuilder.contains( VALUE_COLUMN_NAME, QueryBuilder.bindMarker() );

        this.CONTAINS_STATEMENT = session.prepare( countInTable.where( setIsThisSet ).and( whereContainsValue ) );
        this.SIZE_STATEMENT = session.prepare( countInTable.where( setIsThisSet ) );
        this.ADD_STATEMENT = session.prepare(
                QueryBuilder
                        .insertInto( keyspace, table )
                        .value( KEY_COLUMN_NAME, mappedSetId )
                        .value( VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) );

        this.DELETE_STATEMENT = session.prepare(
                QueryBuilder
                        .delete( KEY_COLUMN_NAME, VALUE_COLUMN_NAME )
                        .from( keyspace, table )
                        .where( setIsThisSet )
                        .and( QueryBuilder.eq( VALUE_COLUMN_NAME, QueryBuilder.bindMarker() ) ) );

        this.GET_PAGE_STATEMENT = session.prepare(
                QueryBuilder
                        .select( VALUE_COLUMN_NAME )
                        .from( keyspace, table )
                        .where( setIsThisSet ) );

    }

    @Override
    public int size() {
        ResultSet execute = session.execute( SIZE_STATEMENT.bind() );
        return getCountResult( execute );
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains( Object o ) {
        return containsValue( (T) o );
    }

    public boolean containsValue( T value ) {
        ResultSet execute;
        try {
            execute = session.execute( CONTAINS_STATEMENT.bind( typeMapper.toBytes( value ) ) );
            int results = getCountResult( execute );
            return results == 1;
        } catch ( MappingException e ) {
            logger.error( MAPPING_ERROR, e );
        }
        return false;
    }

    private static int getCountResult( ResultSet resultSet ) {
        Row one = resultSet.one();
        return one.getInt( CassandraQueryConstants.COUNT_RESULT_COLUMN_NAME );
    }

    @Override
    public Iterator<T> iterator() {
        return new SetProxyIterator( session );
    }

    public class SetProxyIterator implements Iterator<T> {

        private final Iterator<Row> internalIterator;

        public SetProxyIterator( Session session ) {
            ResultSet currentResultPage = session.execute( GET_PAGE_STATEMENT.bind() );
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
            ByteBuffer bytes = row.getBytes( CassandraQueryConstants.VALUE_RESULT_COLUMN_NAME );
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
            byte[] bytes = typeMapper.toBytes( e );
            // add to the set as a new row
            session.execute( ADD_STATEMENT.bind( bytes ) );
            return true;
        } catch ( MappingException e1 ) {
            logger.error( MAPPING_ERROR, e1 );
        }
        return false;
    }

    @Override
    public boolean remove( Object o ) {
        try {
            byte[] bytes = typeMapper.toBytes( (T) o );
            session.execute( DELETE_STATEMENT.bind( bytes ) );
            return true;
        } catch ( MappingException e ) {
            logger.error( MAPPING_ERROR, e );
        }
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        throw new UnsupportedOperationException( UNSTABLE_API_EXCEPTION );
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        // TODO: make this efficient
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

}
