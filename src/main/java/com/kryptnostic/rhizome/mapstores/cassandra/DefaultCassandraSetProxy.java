package com.kryptnostic.rhizome.mapstores.cassandra;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Preconditions;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class DefaultCassandraSetProxy<K, T> extends BaseCassandraSetProxy<K, T> {
    private static final Logger                             logger  = LoggerFactory.getLogger( DefaultCassandraSetProxy.class );

    final SelfRegisteringValueMapper<T>                     typeMapper;

    private static final String                             MAPPING_ERROR          = "Exception while mapping";

    private final PreparedStatement     CONTAINS_STATEMENT;
    private final PreparedStatement     ADD_STATEMENT;
    private final PreparedStatement     DELETE_STATEMENT;

    private final String                                    setId;
    private final Class<T>                                  innerClass;



    public DefaultCassandraSetProxy(
            Session session,
            String keyspace,
            String table,
            String mappedSetId,
            Class<T> innerClass,
            SelfRegisteringValueMapper<T> typeMapper ) {
        super(
                session,
                keyspace,
                table,
                getStatement( keyspace, table, mappedSetId ),
                size( keyspace, table, mappedSetId ) );

        // use cluster.newSession() here to avoid having to connect to cassandra on object creation
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
    }

    private static Select.Where size( String keyspace, String table, String mappedSetId ) {
        return QueryBuilder.select().countAll()
                .from( keyspace, table )
                .where( QueryBuilder.eq( KEY_COLUMN_NAME, mappedSetId ) );
    }

    private static Select.Where getStatement( String keyspace, String table, String mappedSetId ) {
        return QueryBuilder.select( VALUE_COLUMN_NAME )
                .from( keyspace, table )
                .where( QueryBuilder.eq( KEY_COLUMN_NAME, mappedSetId ) );
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
            execute = session.execute( CONTAINS_STATEMENT.bind( setId, valueToBytes( value ) ) );
            int results = getCountResult( execute );
            return results == 1;
        } catch ( MappingException e ) {
            logger.error( MAPPING_ERROR, e );
        }
        return false;
    }

    @Override
    public boolean add( T e ) {
        if ( contains( e ) ) {
            return false;
        }
        try {
            // add to the set as a new row
            session.execute( ADD_STATEMENT.bind( setId, valueToBytes( e ) ) );
            return true;
        } catch ( MappingException e1 ) {
            logger.error( MAPPING_ERROR, e1 );
        }
        return false;
    }

    @Override
    public boolean remove( Object o ) {
        try {
            session.execute( DELETE_STATEMENT.bind( setId, valueToBytes( (T) o ) ) );
            return true;
        } catch ( MappingException e ) {
            logger.error( MAPPING_ERROR, e );
        }
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        for ( Object x : c ) {
            if ( !contains( x ) ) {
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

    private ByteBuffer valueToBytes( T value ) throws MappingException {
        return ByteBuffer.wrap( typeMapper.toBytes( value ) );
    }

    /**
     * @return the typeMapper
     */
    public SelfRegisteringValueMapper<T> getTypeMapper() {
        return typeMapper;
    }

    public String getSetId() {
        return setId;
    }

    @Override
    public Class<T> getTypeClazz() {
        return innerClass;
    }

    @Override
    protected T mapRowToValue( Row row ) {
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
