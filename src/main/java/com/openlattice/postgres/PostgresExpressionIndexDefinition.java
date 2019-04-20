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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Used for specifying custom expression indexes. As the expression index is user provided we don't do any validation
 * unlike with column indexes. Creates indexes concurrently by default.
 */
public class PostgresExpressionIndexDefinition implements PostgresIndexDefinition {
    private final PostgresTableDefinition table;
    private final String                  expression;

    private Optional<String>    name   = Optional.empty();
    private Optional<IndexType> method = Optional.empty();

    private boolean unique     = false;
    private boolean nullsFirst = false;
    private boolean nullsLast  = false;
    private boolean asc        = false;
    private boolean desc       = false;

    private boolean ifNotExists = false;
    private boolean concurrent  = true;

    public PostgresExpressionIndexDefinition( PostgresTableDefinition table, String expression ) {
        this.table = table;
        this.expression = expression;
    }

    @Override public List<PostgresColumnDefinition> getColumns() {
        return ImmutableList.of();
    }

    @Override public boolean isUnique() {
        return unique;
    }

    @Override public boolean isNullsFirst() {
        return nullsFirst;
    }

    @Override public boolean isNullsLast() {
        return nullsLast;
    }

    @Override public boolean isAsc() {
        return asc;
    }

    @Override public boolean isDesc() {
        return desc;
    }

    @Override public Optional<String> getName() {
        return name;
    }

    @Override public Optional<IndexType> getMethod() {
        return method;
    }

    @Override public boolean isConcurrent() {
        return concurrent;
    }

    @Override public boolean isIfNotExists() {
        return ifNotExists;
    }

    @Override public PostgresIndexDefinition name( String name ) {
        this.name = Optional.of( name );
        return this;
    }

    @Override public PostgresIndexDefinition method( IndexType method ) {
        this.method = Optional.of( method );
        return this;
    }

    @Override public PostgresIndexDefinition unique() {
        unique = true;
        return this;
    }

    @Override public PostgresIndexDefinition nullsFirst() {
        checkState( !nullsLast, "Cannot set both nulls first and nulls last at the same time." );
        nullsFirst = true;
        return this;
    }

    @Override public PostgresIndexDefinition nullsLast() {
        checkState( !nullsFirst, "Cannot set both nulls last and nulls first at the same time." );
        nullsLast = true;
        return this;
    }

    @Override public PostgresIndexDefinition asc() {
        checkState( !desc, "Cannot set both ascending and descending at the same time." );
        asc = true;
        return this;
    }

    @Override public PostgresIndexDefinition desc() {
        checkState( !asc, "Cannot set both descending and ascending at the same time." );
        desc = true;
        return this;
    }

    @Override public PostgresIndexDefinition concurrent() {
        concurrent = true;
        return this;
    }

    @Override
    public PostgresIndexDefinition notConcurrent() {
        concurrent = false;
        return this;
    }

    @Override public PostgresIndexDefinition ifNotExists() {
        checkState( name.isPresent(), "Name must be present for if not exists clause" );
        this.ifNotExists = true;
        return this;
    }

    @Override public String sql() {
        validate();
        StringBuilder psql = new StringBuilder( "CREATE " );

        if ( unique ) {
            psql.append( " UNIQUE " );
        }

        psql.append( " INDEX " );

        if ( concurrent ) {
            psql.append( " CONCURRENTLY " );
        }

        if ( ifNotExists ) {
            psql.append( " IF NOT EXISTS " ).append( name.get() );
        }

        psql.append( " ON " ).append( table.getName() );

        if ( method.isPresent() ) {
            psql.append( " USING " ).append( method.get() );
        }

        psql.append( " (" ).append( expression ).append( ") " );

        return psql.toString();
    }

    @Override public void validate() {
        if ( ifNotExists ) {
            checkState( name.isPresent() && StringUtils.isNotBlank( name.get() ),
                    "Name must be present if not exists is specified. See https://www.postgresql.org/docs/9.5/static/sql-createindex.html" );
        }
    }

    @Override public String toString() {
        return "PostgresIndexDefinition{" +
                "table=" + table.getName() +
                ", name=" + name +
                ", method=" + method +
                ", unique=" + unique +
                ", nullsFirst=" + nullsFirst +
                ", nullsLast=" + nullsLast +
                ", asc=" + asc +
                ", desc=" + desc +
                ", ifNotExists=" + ifNotExists +
                ", concurrent=" + concurrent +
                '}';
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof PostgresExpressionIndexDefinition ) ) { return false; }

        PostgresExpressionIndexDefinition that = (PostgresExpressionIndexDefinition) o;

        if ( unique != that.unique ) { return false; }
        if ( nullsFirst != that.nullsFirst ) { return false; }
        if ( nullsLast != that.nullsLast ) { return false; }
        if ( asc != that.asc ) { return false; }
        if ( desc != that.desc ) { return false; }
        if ( ifNotExists != that.ifNotExists ) { return false; }
        if ( concurrent != that.concurrent ) { return false; }
        if ( table != null ? !table.equals( that.table ) : that.table != null ) { return false; }
        if ( name != null ? !name.equals( that.name ) : that.name != null ) { return false; }
        return method != null ? method.equals( that.method ) : that.method == null;
    }

    @Override public int hashCode() {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        result = 31 * result + ( method != null ? method.hashCode() : 0 );
        result = 31 * result + ( unique ? 1 : 0 );
        result = 31 * result + ( nullsFirst ? 1 : 0 );
        result = 31 * result + ( nullsLast ? 1 : 0 );
        result = 31 * result + ( asc ? 1 : 0 );
        result = 31 * result + ( desc ? 1 : 0 );
        result = 31 * result + ( ifNotExists ? 1 : 0 );
        result = 31 * result + ( concurrent ? 1 : 0 );
        return result;
    }
}
