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

package com.kryptnostic.rhizome.cassandra;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class CassandraMaterializedViewBuilder extends CassandraTableBuilder {
    private final CassandraTableBuilder base;
    private final String                name;

    public CassandraMaterializedViewBuilder(
            CassandraTableBuilder base,
            String name ) {
        super( base.getKeyspace().orNull(), name );
        this.base = base;
        this.name = name;
    }

    @Override public CassandraMaterializedViewBuilder partitionKey( ColumnDef... columns ) {
        super.partitionKey( columns );
        return this;
    }

    @Override public CassandraMaterializedViewBuilder clusteringColumns( ColumnDef... columns ) {
        super.clusteringColumns( columns );
        return this;
    }

    @Override public CassandraTableBuilder columns( ColumnDef... columns ) {
        return super.columns( columns );
    }

    @Override public CassandraTableBuilder staticColumns( ColumnDef... columns ) {
        return super.staticColumns( columns );
    }

    //TODO: There are functions such as secondary index which need to be ignore
    @Override public String buildCreateTableQuery() {
        Set<String> baseCols = base.primaryKeyColumns().collect( Collectors.toSet() );
        Set<String> viewCols = primaryKeyColumns().collect( Collectors.toSet() );
        Set<String> addedCols = ImmutableSet.copyOf( Sets.difference( viewCols, baseCols ) );
        Set<String> missingCols = ImmutableSet.copyOf( Sets.difference( baseCols, viewCols ) );

        Preconditions.checkState( missingCols.isEmpty(),
                "Missing columns: " + missingCols.stream().collect( Collectors.joining( "," ) ) );

        Preconditions.checkState( addedCols.size() <= 1, "Can only add at mose one column to primary" );

        StringBuilder query = new StringBuilder( "CREATE MATERIALIZED VIEW " )
                .append( ifNotExists ? "IF NOT EXISTS " : "" )
                .append( base.getKeyspace().transform( ks -> ks + "." + name ).or( name ) )
                .append( " AS\n" )
                .append( "SELECT " );

        query.append( primaryKeyColumns()
                .collect( Collectors.joining( "," ) ) );
        query
                .append( "\nFROM " )
                .append( base.getKeyspace().transform( ks -> ks + "." + base.getName() ).or( base.getName() ) )
                .append( "\nWHERE " );

        query.append( primaryKeyColumns()
                .map( col -> col + " IS NOT NULL" )
                .collect( Collectors.joining( " AND " ) ) );

        query
                .append( "\nPRIMARY KEY ((" )
                .append( Stream.of( partition ).map( ColumnDef::cql ).collect( Collectors.joining( "," ) ) )
                .append( ")," )
                .append( Stream.of( clustering ).map( ColumnDef::cql ).collect( Collectors.joining( "," ) ) )
                .append( ")" );

        return query.toString();
    }

}
