/*
 * Copyright (C) 2017. OpenLattice, Inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 */

package com.openlattice.postgres;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresTableDefinition implements TableDefinition {
    protected static final Logger logger = LoggerFactory.getLogger( PostgresTableDefinition.class );

    private final String                                  name;
    private final LinkedHashSet<PostgresColumnDefinition> primaryKey = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresColumnDefinition> columns    = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresColumnDefinition> unique     = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresIndexDefinition>  indexes    = new LinkedHashSet<>();

    private final Map<String, PostgresColumnDefinition> columnMap = Maps.newHashMap();

    protected boolean unlogged;
    protected boolean ifNotExists         = true;
    protected boolean overwriteOnConflict = false;
    protected boolean temporary = false;

    public PostgresTableDefinition( String name ) {
        this.name = name;
    }

    public PostgresTableDefinition temporary() {
        this.temporary = true;
        return this;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public PostgresTableDefinition addColumns( PostgresColumnDefinition... columnsToAdd ) {
        List<PostgresColumnDefinition> colList = Arrays.asList( columnsToAdd );
        colList.stream().forEach( col -> columnMap.put( col.getName(), col ) );
        this.columns.addAll( colList );
        return this;
    }

    public PostgresTableDefinition addIndexes( PostgresIndexDefinition... indexes ) {
        this.indexes.addAll( Arrays.asList( indexes ) );
        return this;
    }

    public PostgresTableDefinition overwriteOnConflict() {
        this.overwriteOnConflict = true;
        return this;
    }

    public PostgresTableDefinition unlogged() {
        this.unlogged = true;
        return this;
    }

    public String getName() {
        return name;
    }

    public LinkedHashSet<PostgresColumnDefinition> getPrimaryKey() {
        if ( primaryKey.isEmpty() ) {
            //Try columns.
            return columns.stream()
                    .filter( PostgresColumnDefinition::isPrimaryKey )
                    .collect( Collectors.toCollection( LinkedHashSet::new ) );
        }
        return primaryKey;
    }

    public PostgresTableDefinition primaryKey( PostgresColumnDefinition... primaryKeyColumns ) {
        checkNotNull( primaryKeyColumns, "Cannot set null primary key" );
        /*
         * Technically this will allow you to set several empty primary keys which are all equivalent.
         * This will allow
         */
        checkState( primaryKey.isEmpty(), "Primary key has already been set." );
        primaryKey.addAll( Arrays.asList( primaryKeyColumns ) );
        return this;
    }

    public LinkedHashSet<PostgresColumnDefinition> getColumns() {
        return columns.stream()
                .filter( c -> !c.isPrimaryKey() )
                .collect( Collectors.toCollection( LinkedHashSet::new ) );
    }

    public LinkedHashSet<PostgresColumnDefinition> getUnique() {
        return unique;
    }

    public PostgresTableDefinition setUnique( PostgresColumnDefinition... uniqueColumns ) {
        checkNotNull( uniqueColumns, "Cannot set null unique columns" );
        /*
         * Technically this will allow you to set several empty primary keys which are all equivalent.
         * This will allow
         */
        checkState( unique.isEmpty(), "Primary key has already been set." );
        unique.addAll( Arrays.asList( uniqueColumns ) );
        return this;
    }

    public LinkedHashSet<PostgresIndexDefinition> getIndexes() {
        return indexes;
    }

    public PostgresColumnDefinition getColumn( String name ) {
        return columnMap.get( name );
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    @Override
    public String createTableQuery() {
        validate();
        StringBuilder ctb = new StringBuilder( "CREATE " );

        if( temporary ) {
            ctb.append( "TEMPORARY " );
        }

        if ( unlogged ) {
            ctb.append( "UNLOGGED " );
        }

        ctb.append( "TABLE " );

        if ( ifNotExists ) {
            ctb.append( " IF NOT EXISTS " );
        }

        String columnSql = columns.stream()
                .map( PostgresColumnDefinition::sql )
                .collect( Collectors.joining( "," ) );

        ctb.append( name ).append( " (" ).append( columnSql );

        if ( !primaryKey.isEmpty() ) {
            String pkSql = primaryKey.stream()
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.joining( ", " ) );
            ctb.append( ", PRIMARY KEY (" ).append( pkSql ).append( " )" );

        }

        if ( !unique.isEmpty() ) {
            String uSql = unique.stream()
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.joining( "," ) );
            ctb.append( ", UNIQUE (" ).append( uSql ).append( " )" );

        }

        ctb.append( ")" );
        return ctb.toString();
    }

    public String insertQuery( PostgresColumnDefinition... requestedColumns ) {
        return insertQuery( Optional.empty(), Arrays.asList( requestedColumns ) );
    }

    public String insertQuery( Optional<String> onConflict, List<PostgresColumnDefinition> requestedColumns ) {
        if ( this.columns.containsAll( requestedColumns ) ) {
            StringBuilder insertSql = new StringBuilder( "INSERT INTO " ).append( name );

            Collection<PostgresColumnDefinition> insertCols = requestedColumns.isEmpty() ? columns : requestedColumns;

            if ( !requestedColumns.isEmpty() ) {
                insertSql.append( " (" )
                        .append( requestedColumns.stream().map( PostgresColumnDefinition::getName )
                                .collect( Collectors.joining( "," ) ) )
                        .append( ")" );
            }
            insertSql.append( " VALUES (" )
                    .append( insertCols.stream()
                            .map( col -> col.getDatatype().equals( PostgresDatatype.JSONB ) ? "?::jsonb" : "?" )
                            .collect( Collectors.joining( ", " ) ) )
                    .append( ") " );

            onConflict.ifPresent( insertSql::append );

            return insertSql.toString();
        } else {
            List<String> missingColumns = requestedColumns.stream()
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }
    }

    public String updateQuery(
            List<PostgresColumnDefinition> whereToUpdate,
            List<PostgresColumnDefinition> columnsToUpdate ) {
        return updateQuery( whereToUpdate, columnsToUpdate, true );
    }

    public String updateQuery(
            List<PostgresColumnDefinition> whereToUpdate,
            List<PostgresColumnDefinition> columnsToUpdate,
            boolean notOnConflict ) {
        checkArgument( !columnsToUpdate.isEmpty(), "Columns to update must be specified." );
        checkArgument( !whereToUpdate.isEmpty(), "Columns for where clause must be specified." );

        if ( this.columns.containsAll( columnsToUpdate ) && this.columns.containsAll( whereToUpdate ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder updateSql = new StringBuilder( "UPDATE " );
            if ( notOnConflict ) {
                updateSql.append( name );
            }

            updateSql.append( " SET " ).append( getBindParamsForCols( columnsToUpdate, false ) );

            if ( notOnConflict ) {
                updateSql.append( " WHERE " ).append( getBindParamsForCols( whereToUpdate, true ) );
            }

            return updateSql.toString();
        } else {
            List<String> missingColumns = Stream.concat( columnsToUpdate.stream(), whereToUpdate.stream() )
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table " + getName() + "is missing requested columns: " + missingColumns;
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }
    }

    public String deleteQuery( List<PostgresColumnDefinition> whereToDelete ) {
        checkArgument( !whereToDelete.isEmpty(), "Columns for where clause must be specified." );

        if ( this.columns.containsAll( whereToDelete ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder deleteSql = new StringBuilder( "DELETE FROM " ).append( name );
            deleteSql.append( " WHERE " ).append( getBindParamsForCols( whereToDelete, true ) );

            return deleteSql.toString();
        } else {
            List<String> missingColumns = whereToDelete.stream()
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }

    }

    public String selectQuery( List<PostgresColumnDefinition> columnsToSelect ) {
        return selectQuery( columnsToSelect, ImmutableList.of() );
    }

    public String selectQuery(
            List<PostgresColumnDefinition> columnsToSelect,
            List<PostgresColumnDefinition> whereToSelect ) {

        if ( this.columns.containsAll( columnsToSelect ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder selectSql = new StringBuilder( "SELECT " );
            if ( columnsToSelect.isEmpty() ) {
                selectSql.append( "*" );
            } else {
                selectSql.append( columnsToSelect.stream()
                        .map( PostgresColumnDefinition::getName )
                        .collect( Collectors.joining( ", " ) ) );
            }
            selectSql.append( " FROM " ).append( name );
            if ( !whereToSelect.isEmpty() ) {
                selectSql.append( " WHERE " ).append( getBindParamsForCols( whereToSelect, true ) );
            }
            return selectSql.toString();
        } else {
            List<String> missingColumns = Stream.concat( columnsToSelect.stream(), whereToSelect.stream() )
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + missingColumns;
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }
    }

    public String selectInQuery(
            List<PostgresColumnDefinition> columnsToSelect,
            List<PostgresColumnDefinition> whereToSelect ) {
        return selectInQuery( columnsToSelect, whereToSelect, 1 );
    }

    public String selectInQuery(
            List<PostgresColumnDefinition> columnsToSelect,
            List<PostgresColumnDefinition> whereToSelect, int batchSize ) {
        checkState( !whereToSelect.isEmpty(), "where columns must be specified." );
        if ( this.columns.containsAll( columnsToSelect ) ) {
            //TODO: Warn when where clause is unindexed and will trigger a table scan.
            StringBuilder selectSql = new StringBuilder( "SELECT " );
            if ( columnsToSelect.isEmpty() ) {
                selectSql.append( "*" );
            } else {
                selectSql.append( columnsToSelect.stream()
                        .map( PostgresColumnDefinition::getName )
                        .collect( Collectors.joining( ", " ) ) );
            }

            selectSql.append( " FROM " ).append( name );

            final String compoundElement = "(" + StringUtils.repeat( "?", ",", whereToSelect.size() ) + ")";
            final String batched = StringUtils.repeat( compoundElement, ",", batchSize );

            selectSql.append( " WHERE (" )
                    .append( whereToSelect.stream()
                            .map( PostgresColumnDefinition::getName )
                            .collect( Collectors.joining( "," ) ) )
                    .append( ") IN (" )
                    .append( batched )
                    .append( ")" );

            return selectSql.toString();
        } else {
            List<String> missingColumns = Stream.concat( columnsToSelect.stream(), whereToSelect.stream() )
                    .filter( c -> !this.columns.contains( c ) )
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.toList() );
            String errMsg = "Table is missing requested columns: " + String.valueOf( missingColumns );
            logger.error( errMsg );
            throw new IllegalArgumentException( errMsg );
        }
    }

    @Override
    public Stream<String> getCreateIndexQueries() {
        return indexes.stream().map( PostgresIndexDefinition::sql );
    }

    protected void validate() {
        columns.stream()
                .collect( Collectors.groupingBy( PostgresColumnDefinition::getName ) )
                .forEach( ( lhs, rhs ) -> checkState( rhs.size() == 1,
                        "Detected duplicate column: %s", lhs ) );

        primaryKey.stream()
                .collect( Collectors.groupingBy( PostgresColumnDefinition::getName ) )
                .forEach( ( lhs, rhs ) -> checkState( rhs.size() == 1,
                        "Detected duplicate primary key column: %s",
                        lhs ) );

        //TODO: Add validation on indices.

    }

    @Override public String toString() {
        return "PostgresTableDefinition{" +
                "name='" + name + '\'' +
                ", primaryKey=" + primaryKey +
                ", columns=" + columns +
                ", unique=" + unique +
                ", indexes=" + indexes +
                ", ifNotExists=" + ifNotExists +
                '}';
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof PostgresTableDefinition ) ) { return false; }

        PostgresTableDefinition that = (PostgresTableDefinition) o;

        if ( ifNotExists != that.ifNotExists ) { return false; }
        if ( name != null ? !name.equals( that.name ) : that.name != null ) { return false; }
        if ( primaryKey != null ? !primaryKey.equals( that.primaryKey ) : that.primaryKey != null ) { return false; }
        if ( columns != null ? !columns.equals( that.columns ) : that.columns != null ) { return false; }
        if ( unique != null ? !unique.equals( that.unique ) : that.unique != null ) { return false; }
        return indexes != null ? indexes.equals( that.indexes ) : that.indexes == null;
    }

    @Override public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + ( primaryKey != null ? primaryKey.hashCode() : 0 );
        result = 31 * result + ( columns != null ? columns.hashCode() : 0 );
        result = 31 * result + ( unique != null ? unique.hashCode() : 0 );
        result = 31 * result + ( ifNotExists ? 1 : 0 );
        return result;
    }

    private String getBindParamsForCols(
            Collection<PostgresColumnDefinition> columns,
            boolean isBindingWhere ) {
        String joinString = isBindingWhere ? " and " : ", ";
        return columns.stream().map( column -> {
            if ( overwriteOnConflict && !isBindingWhere ) {
                return column.getName() + " = EXCLUDED." + column.getName();
            }
            String eq = column.getDatatype().equals( PostgresDatatype.JSONB ) ?
                    " = ?::jsonb " :
                    " = ? ";
            return column.getName() + eq;
        } ).collect( Collectors.joining( joinString ) );
    }
}
