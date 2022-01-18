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

package com.geekbeast.postgres;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class  PostgresColumnDefinition {
    private final String           name;
    private final PostgresDatatype datatype;
    private       boolean          primaryKey             = false;
    private boolean                            unique                 = false;
    private boolean                            notNull                = false;
    private Optional<?>                        defaultValue           = Optional.empty();
    private Optional<PostgresTableDefinition>  foreignTableReference  = Optional.empty();
    private Optional<PostgresColumnDefinition> foreignColumnReference = Optional.empty();

    public PostgresColumnDefinition( String name, PostgresDatatype datatype ) {
        this.name = name;
        this.datatype = datatype;
    }

    public String getName() {
        return name;
    }

    public PostgresDatatype getDatatype() {
        return datatype;
    }

    public @Nonnull PostgresColumnDefinition unique() {
        unique = true;
        return this;
    }

    public PostgresColumnDefinition primaryKey() {
        primaryKey = true;
        return this;
    }

    public PostgresColumnDefinition foreignKey( PostgresTableDefinition tableReference ) {
        this.foreignTableReference = Optional.of( tableReference );
        return this;
    }

    public PostgresColumnDefinition foreignKey(
            PostgresTableDefinition tableReference,
            PostgresColumnDefinition columnReference ) {
        this.foreignTableReference = Optional.of( tableReference );
        this.foreignColumnReference = Optional.of( columnReference );
        return this;
    }

    public PostgresColumnDefinition withDefault( Object defaultValue ) {
        this.defaultValue = Optional.of( defaultValue );
        return this;
    }

    public @Nonnull PostgresColumnDefinition notNull() {
        this.notNull = true;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String sql() {
        StringBuilder pcdSql = new StringBuilder( name );
        pcdSql.append( " " ).append( datatype.sql() );

        if ( primaryKey ) {
            pcdSql.append( " PRIMARY KEY " );
        }

        if ( unique ) {
            pcdSql.append( " UNIQUE " );
        }

        if ( notNull ) {
            pcdSql.append( " NOT NULL " );
        }

        foreignTableReference.ifPresent( postgresTableDefinition -> pcdSql.append( " REFERENCES " )
                .append( postgresTableDefinition.getName() ) );

        //foreignKey(...) ensure that if foreign column reference is present that foreignTable is present
        foreignColumnReference.ifPresent( postgresColumnDefinition -> pcdSql.append( " (" )
                .append( postgresColumnDefinition.getName() ).append( ") " ) );

        defaultValue.ifPresent( o -> pcdSql.append( " default " ).append( String.valueOf( o ) ) );

        return pcdSql.toString().trim();
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof PostgresColumnDefinition ) ) { return false; }
        PostgresColumnDefinition that = (PostgresColumnDefinition) o;
        return primaryKey == that.primaryKey &&
                unique == that.unique &&
                notNull == that.notNull &&
                Objects.equals( name, that.name ) &&
                datatype == that.datatype;
    }

    @Override public int hashCode() {

        return Objects.hash( name, datatype, primaryKey, unique, notNull );
    }

    @Override public String toString() {
        return "PostgresColumnDefinition{" +
                "name='" + name + '\'' +
                ", datatype=" + datatype +
                ", primaryKey=" + primaryKey +
                ", unique=" + unique +
                ", notNull=" + notNull +
                ", defaultValue=" + defaultValue +
                ", foreignTableReference=" + foreignTableReference +
                ", foreignColumnReference=" + foreignColumnReference +
                '}';
    }

    public void validate() {
    }
}
