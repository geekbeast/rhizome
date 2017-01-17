package com.kryptnostic.rhizome.cassandra;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.BindMarker;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Builder;
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
    private ColumnDef[]                   staticColumns     = new ColumnDef[] {};
    private ColumnDef[]                   secondaryIndices  = new ColumnDef[] {};
    private ColumnDef[]                   fullCollectionIndices  = new ColumnDef[] {};
    private ColumnDef[]                   sasi              = new ColumnDef[] {};
    private Function<ColumnDef, DataType> typeResolver      = c -> c.getType();
    private int                           replicationFactor = 2;

    // array of clustering columns with clustering order DESC. Only contiguous subarray of clustering columns from the
    // beginning would work for now.
    private ColumnDef[]                   desc              = new ColumnDef[] {};

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

    public CassandraTableBuilder staticColumns( ColumnDef... columns ) {
        this.staticColumns = Preconditions.checkNotNull( columns );
        Arrays.asList( columns ).forEach( Preconditions::checkNotNull );
        return this;
    }

    public CassandraTableBuilder secondaryIndex( ColumnDef... columns ) {
        this.secondaryIndices = Preconditions.checkNotNull( columns );
        return this;
    }

    public CassandraTableBuilder fullCollectionIndex( ColumnDef... columns ) {
        this.fullCollectionIndices = Preconditions.checkNotNull( columns );
        return this;
    }

    public CassandraTableBuilder sasi( ColumnDef... columns ) {
        if ( Iterables.any( Arrays.asList( columns ), col -> col.getType().isCollection() ) ) {
            throw new IllegalArgumentException( "Cannot create sasi index on collection columns" );
        }
        this.sasi = columns;
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

    public CassandraTableBuilder withReplicationFactor( int replicationFactor ) {
        this.replicationFactor = replicationFactor;
        return this;
    }

    public CassandraTableBuilder withDescendingOrder( ColumnDef... columns ) {
        this.desc = columns;
        return this;
    }

    /**
     * Builds the set of queries necessary to create the cassandra table and all secondary indices.
     * 
     * @return A list of CQL queries that MUST be executed in provided order to create the table.
     * 
     */
    public List<String> build() {
        List<Supplier<Stream<String>>> queries = Arrays
                .<Supplier<Stream<String>>> asList(
                        () -> Arrays.asList( this.buildCreateTableQuery() ).stream(),
                        this::buildRegularIndexQueries,
                        this::buildFullCollectionIndexQueries,
                        this::buildSasiIndexQueries );
        return queries.stream().flatMap( Supplier::get ).collect( Collectors.toList() );
    }

    public Stream<String> buildSasiIndexQueries() {
        return Arrays.asList( sasi ).stream().map( createSasiIndexQueryFunction( keyspace.get(), name ) );
    }

    public Stream<String> buildRegularIndexQueries() {
        return Arrays.asList( secondaryIndices ).stream()
                .map( createRegularSecondaryIndexQueryFunction( keyspace.get(), name ) );
    }

    public Stream<String> buildFullCollectionIndexQueries() {
        return Arrays.asList( fullCollectionIndices ).stream()
                .map( createFullCollectionIndexQueryFunction( keyspace.get(), name ) );
    }

    public String buildCreateTableQuery() {
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

        if ( staticColumns.length > 0 ) {
            appendStaticColumnDefs( query, staticColumns );
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

        if ( desc.length > 0 ) {
            query.append( " WITH CLUSTERING ORDER BY ( " );
            appendClusteringOrder( query, clustering, desc );
            query.append( " )" );
        }
        return query.toString();
    }

    public Select buildLoadAllPartitionKeysQuery() {
        Builder s = QueryBuilder.select( Iterables.toArray( partitionKeyColumns(), String.class ) ).distinct();
        return keyspace.isPresent() ? s.from( keyspace.get(), name ) : s.from( name );
    }

    public RegularStatement buildLoadAllPrimaryKeysQuery() {
        Builder s = QueryBuilder.select( Iterables.toArray( primaryKeyColumns(), String.class ) );
        return keyspace.isPresent() ? s.from( keyspace.get(), name ) : s.from( name );
    }

    public Select buildLoadAllQuery() {
        Builder s = QueryBuilder.select( Iterables.toArray( allColumns(), String.class ) );
        return keyspace.isPresent() ? s.from( keyspace.get(), name ) : s.from( name );
    }

    public Where buildLoadQuery() {
        return buildLoadQuery( primaryKeyColumnDefs() );
    }

    public Where buildLoadByPartitionKeyQuery() {
        return buildLoadQuery( partitionKeyColumnDefs() );
    }

    private Where buildLoadQuery( Iterable<ColumnDef> selectedCols ) {
        Where w = buildLoadAllQuery().where();
        for ( ColumnDef col : selectedCols ) {
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
        return buildDeleteQuery( primaryKeyColumnDefs() );
    }

    public com.datastax.driver.core.querybuilder.Delete.Where buildDeleteByPartitionKeyQuery() {
        return buildDeleteQuery( partitionKeyColumnDefs() );
    }

    private com.datastax.driver.core.querybuilder.Delete.Where buildDeleteQuery( Iterable<ColumnDef> selectedCols ) {
        Delete del;
        if ( keyspace.isPresent() ) {
            del = QueryBuilder.delete().from( keyspace.get(), name );
        } else {
            del = QueryBuilder.delete().from( name );
        }

        com.datastax.driver.core.querybuilder.Delete.Where w = del.where();
        for ( ColumnDef col : selectedCols ) {
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

    private Iterable<ColumnDef> partitionKeyColumnDefs() {
        return Arrays.asList( this.partition );
    }

    private Iterable<ColumnDef> primaryKeyColumnDefs() {
        return Iterables.concat( Arrays.asList( this.partition ), Arrays.asList( this.clustering ) );
    }

    public Iterable<String> primaryKeyColumns() {
        return Iterables.transform( primaryKeyColumnDefs(), ColumnDef::cql );
    }

    private Iterable<String> partitionKeyColumns() {
        return Iterables.transform( Arrays.asList( this.partition ), ColumnDef::cql );
    }

    private Iterable<String> allColumns() {
        return Iterables.transform( Iterables.concat( Arrays.asList( this.partition ),
                Arrays.asList( this.clustering ),
                Arrays.asList( this.columns ) ), ColumnDef::cql );
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

    private StringBuilder appendStaticColumnDefs(
            StringBuilder builder,
            ColumnDef[] columns ) {
        for ( int i = 0; i < columns.length; ++i ) {
            builder
                    .append( columns[ i ].cql() )
                    .append( " " )
                    .append( columns[ i ].getType( typeResolver ).toString() )
                    .append( " STATIC" )
                    .append( "," );
        }
        return builder;
    }

    private StringBuilder appendClusteringOrder(
            StringBuilder builder,
            ColumnDef[] clustering,
            ColumnDef[] desc ) {
        Set<String> descNames = Arrays.stream( desc ).map( col -> col.cql() ).collect( Collectors.toSet() );

        int len = clustering.length - 1;
        for ( int i = 0; i < len; i++ ) {
            builder.append( clustering[ i ].cql() )
                    .append( " " )
                    .append( descNames.contains( clustering[ i ].cql() ) ? "DESC" : "ASC" )
                    .append( "," );
        }
        builder.append( clustering[ len ].cql() )
                .append( " " )
                .append( descNames.contains( clustering[ len ].cql() ) ? "DESC" : "ASC" );
        return builder;
    };

    public Optional<String> getKeyspace() {
        return keyspace;
    }

    private static Function<ColumnDef, String> createRegularSecondaryIndexQueryFunction(
            String keyspace,
            String table ) {
        return column -> createRegularSecondaryIndexQuery( keyspace, table, column );
    }

    private static Function<ColumnDef, String> createFullCollectionIndexQueryFunction(
            String keyspace,
            String table ) {
        return column -> createFullCollectionIndexQuery( keyspace, table, column );
    }

    private static final Function<ColumnDef, String> createSasiIndexQueryFunction(
            String keyspace,
            String table ) {
        return column -> createSasiIndexQuery( keyspace, table, column );
    }

    public static String createRegularSecondaryIndexQuery( String keyspace, String table, ColumnDef column ) {
        String query = "CREATE INDEX IF NOT EXISTS ON %s.%s (%s)";
        return String.format( query, keyspace, table, column.cql() );
    }

    public static String createFullCollectionIndexQuery( String keyspace, String table, ColumnDef column ) {
        String query = "CREATE INDEX IF NOT EXISTS ON %s.%s (FULL(%s))";
        return String.format( query, keyspace, table, column.cql() );
    }

    public static final String createSasiIndexQuery(
            String keyspace,
            String table,
            ColumnDef column ) {
        String query = "CREATE CUSTOM INDEX IF NOT EXISTS ON %s.%s (%s) USING 'org.apache.cassandra.index.sasi.SASIIndex'";
        return String.format( query, keyspace, table, column.cql() );
    }
}
