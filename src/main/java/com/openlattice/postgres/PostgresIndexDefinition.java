/*
 * Copyright (C) 2018. OpenLattice, Inc.
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
 *
 */

package com.openlattice.postgres;

import java.util.List;
import java.util.Optional;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public interface PostgresIndexDefinition {
    List<PostgresColumnDefinition> getColumns();

    boolean isUnique();

    boolean isNullsFirst();

    boolean isNullsLast();

    boolean isAsc();

    boolean isDesc();

    Optional<String> getName();

    Optional<IndexType> getMethod();

    boolean isConcurrent();

    boolean isIfNotExists();

    PostgresIndexDefinition name( String name );

    PostgresIndexDefinition method( IndexType method );

    PostgresIndexDefinition unique();

    PostgresIndexDefinition nullsFirst();

    PostgresIndexDefinition nullsLast();

    PostgresIndexDefinition asc();

    PostgresIndexDefinition desc();

    PostgresIndexDefinition concurrent();

    PostgresIndexDefinition notConcurrent();

    PostgresIndexDefinition ifNotExists();

    String sql();

    void validate();
}
