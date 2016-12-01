package com.kryptnostic.rhizome.cassandra;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.BindMarker;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * This class is not thread safe.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class CassandraTableBuilder {

    public static class ValueColumn implements ColumnDef {
        private final String   cql;
        private final DataType dataType;

        public ValueColumn( String cql, DataType dataType ) {
            this.cql = cql;
            this.dataType = dataType;

        }

        public String cql() {
            return cql;
        }

        public DataType getType() {
            return dataType;
        }

        @Override
        public DataType getType( Function<ColumnDef, DataType> typeResolver ) {
            return dataType == null ? typeResolver.apply( this ) : getType();
        }

    }

    private Optional<String>              keyspace          = Optional.absent();
    private final String                  name;
    private boolean                       ifNotExists       = false;
    private ColumnDef[]                   partition         = null;
    private ColumnDef[]                   clustering        = new ColumnDef[] {};
    private ColumnDef[]                   columns           = new ColumnDef[] {};
    private Function<ColumnDef, DataType> typeResolver      = c -> c.getType();
    private int                           replicationFactor = 2;

    public CassandraTableBuilder( TableDef table ) {
        this( table.getKeyspace(), table.getName() );
    }

    @Deprecated
    public CassandraTableBuilder( String keyspace, TableDef table ) {
        this( keyspace, table.getName() );
    }

    public CassandraTableBuilder( String keyspace, String name ) {
        this.keyspace = Optional.fromNullable( keyspace );
        this.name = name;
    }

    public CassandraTableBuilder( String name ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( name ), "Table name cannot be blank." );
        this.name = name;
    }

    public CassandraTableBuilder partitionKey( ColumnDef... columns ) {
        this.partition = Preconditions.checkNotNull( columns );
        Arrays.asList( columns ).forEach( Preconditions::checkNotNull );
        Preconditions.checkArgument( columns.length > 0, "Must specify at least one partition key column." );
        return this;
    }

    public CassandraTableBuilder clusteringColumns( ColumnDef... columns ) {
        this.clustering = Preconditions.checkNotNull( columns );
        Arrays.asList( columns ).forEach( Preconditions::checkNotNull );
        return this;
    }

    public CassandraTableBuilder columns( ColumnDef... columns ) {
        this.columns = Preconditions.checkNotNull( columns );
        Arrays.asList( columns ).forEach( Preconditions::checkNotNull );
        return this;
    }

    public CassandraTableBuilder ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    public CassandraTableBuilder withTypeResolver( Function<ColumnDef, DataType> typeResolver ) {
        this.typeResolver = typeResolver;
        return this;
    }

    CassandraTableBuilder withReplicationFactor( int replicationFactor ) {
        this.replicationFactor = replicationFactor;
        return this;
    }

    public String buildQuery() {
        Preconditions.checkState( partition != null, "Partition key was not configured" );

        // Map<ColumnDef, String> bindMarkers = generateBindMarkers();

        StringBuilder query = new StringBuilder( "CREATE TABLE " );

        if ( ifNotExists ) {
            query.append( "IF NOT EXISTS " );
        }

        if ( keyspace.isPresent() ) {
            query.append( keyspace.get() ).append( "." );
        }

        query.append( name );

        query.append( " ( " );
        appendColumnDefs( query, partition );
        if ( clustering.length > 0 ) {
            appendColumnDefs( query, clustering );
        }

        if ( columns.length > 0 ) {
            appendColumnDefs( query, columns );
        }

        // extra comma from appendColumns is already included
        query.append( " PRIMARY KEY (" );

        // Only add if compound partition key
        if ( this.partition.length > 1 ) {
            query.append( " ( " );
        }

        query.append( getPrimaryKeyDef( partition ) );

        // Only add if compound partition key
        if ( this.partition.length > 1 ) {
            query.append( " ) " );
        }

        if ( clustering.length > 0 ) {
            query.append( ", " );
            query.append( getPrimaryKeyDef( clustering ) );
        }

        query.append( " ) )" );
        return query.toString();
    }

    public Select buildLoadAllQuery() {
        if ( keyspace.isPresent() ) {
            return QueryBuilder.select( Iterables.toArray( allColumns(), String.class ) ).from( keyspace.get(), name );
        } else {
            return QueryBuilder.select( Iterables.toArray( allColumns(), String.class ) ).from( name );
        }
    }

    public Where buildLoadQuery() {
        Where w = buildLoadAllQuery().where();
        for ( ColumnDef col : primaryKeyColumns() ) {
            w = w.and( QueryBuilder.eq( col.cql(), col.bindMarker() ) );
        }
        return w;
    }

    public Insert buildStoreQuery() {
        List<String> cols = ImmutableList.copyOf( allColumns() );
        Object[] markers = new BindMarker[ cols.size() ];

        for ( int i = 0; i < markers.length; ++i ) {
            markers[ i ] = QueryBuilder.bindMarker();
        }

        List<Object> bindMarkers = Arrays.asList( markers );

        Insert insertQuery;

        if ( keyspace.isPresent() ) {
            insertQuery = QueryBuilder.insertInto( keyspace.get(), name );
        } else {
            insertQuery = QueryBuilder.insertInto( name );
        }

        return insertQuery.values( cols, bindMarkers );
    }

    public com.datastax.driver.core.querybuilder.Delete.Where buildDeleteQuery() {
        Delete del;
        if ( keyspace.isPresent() ) {
            del = QueryBuilder.delete().from( keyspace.get(), name );
        } else {
            del = QueryBuilder.delete().from( name );
        }

        com.datastax.driver.core.querybuilder.Delete.Where w = del.where();
        for ( ColumnDef col : primaryKeyColumns() ) {
            w = w.and( QueryBuilder.eq( col.cql(), col.bindMarker() ) );
        }
        return w;
    }

    public String getName() {
        return this.name;
    }

    public int getReplicationFactor() {
        return this.replicationFactor;
    }

    private Iterable<ColumnDef> primaryKeyColumns() {
        return Iterables.concat( Arrays.asList( this.partition ), Arrays.asList( this.clustering ) );
    }

    private Iterable<String> allColumns() {
        return Iterables.transform( Iterables.concat( Arrays.asList( this.partition ),
                Arrays.asList( this.clustering ),
                Arrays.asList( this.columns ) ), colDef -> colDef.cql() );
    }

    private static String getPrimaryKeyDef( ColumnDef[] columns ) {
        StringBuilder builder = new StringBuilder();

        int len = columns.length - 1;
        for ( int i = 0; i < len; ++i ) {
            builder
                    .append( columns[ i ].cql() ).append( "," );
        }
        builder
                .append( columns[ len ].cql() );

        return builder.toString();
    }

    private StringBuilder appendColumnDefs(
            StringBuilder builder,
            ColumnDef[] columns ) {
        for ( int i = 0; i < columns.length; ++i ) {
            builder
                    .append( columns[ i ].cql() )
                    .append( " " )
                    .append( columns[ i ].getType( typeResolver ).toString() )
                    .append( "," );
        }
        return builder;
    }

    public Optional<String> getKeyspace() {
        return keyspace;
    }

}
