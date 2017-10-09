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

package com.com.openlattice.postgres;

import static com.google.common.base.Preconditions.checkState;
import static com.hazelcast.util.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresTableDefinition implements TableDefinition {
    private final String name;
    private final LinkedHashSet<PostgresColumnDefinition> primaryKey = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresColumnDefinition> columns    = new LinkedHashSet<>();
    private final LinkedHashSet<PostgresColumnDefinition> unique     = new LinkedHashSet<>();
    private       LinkedHashSet<PostgresIndexDefinition>  indexes    = new LinkedHashSet<>();

    private boolean ifNotExists = true;

    public PostgresTableDefinition( String name ) {
        this.name = name;
    }

    public PostgresTableDefinition addColumns( PostgresColumnDefinition... columnsToAdd ) {
        this.columns.addAll( Arrays.asList( columnsToAdd ) );
        return this;
    }

    public PostgresTableDefinition addIndexes( PostgresIndexDefinition... indexes ) {
        this.indexes.addAll( Arrays.asList( indexes ) );
        return this;
    }

    public String getName() {
        return name;
    }

    public LinkedHashSet<PostgresColumnDefinition> getPrimaryKey() {
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
        return columns;
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

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    @Override
    public String createTableQuery() {
        validate();
        StringBuilder ctb = new StringBuilder( "CREATE TABLE " );
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
                    .collect( Collectors.joining( "," ) );
            ctb.append( ", PRIMARY KEY (" ).append( pkSql ).append( " )" );

        }

        if ( !unique.isEmpty() ) {
            String uSql = unique.stream()
                    .map( PostgresColumnDefinition::getName )
                    .collect( Collectors.joining( "," ) );
            ctb.append( ", UNIQUE (" ).append( uSql ).append( " )" );

        }

        return ctb.toString();
    }

    @Override
    public Stream<String> getCreateIndexQueries() {
        return indexes.stream().map( PostgresIndexDefinition::sql );
    }

    private void validate() {
        primaryKey.stream()
                .collect( Collectors.groupingBy( PostgresColumnDefinition::getName ) )
                .forEach( ( lhs, rhs ) -> checkState( rhs.size() == 1,
                        "Detected duplicate column: %s", lhs ) );

        columns.stream()
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
        result = 31 * result + ( indexes != null ? indexes.hashCode() : 0 );
        result = 31 * result + ( ifNotExists ? 1 : 0 );
        return result;
    }
}
