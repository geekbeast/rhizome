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

package com.openlattice.postgres

import java.util.*

/**
 *
 * Used to define a partitioned postgres table.
 */
class CitusDistributedTableDefinition(
        name: String,
        val distributionColumn: PostgresColumnDefinition
) : PostgresTableDefinition(name) {
    private var colocationColumn: Optional<PostgresColumnDefinition> = Optional.empty()

    fun colocationColumn(column: PostgresColumnDefinition): CitusDistributedTableDefinition {
        this.colocationColumn = Optional.of(column)
        return this
    }

    fun distributionColumns( column: PostgresColumnDefinition): CitusDistributedTableDefinition {
        if (!this.distributionColumn.isEmpty()) {
            this.distributionColumn.clear()
            PostgresTableDefinition.logger.warn(
                    "Previous partition column specification is being overriden. This is unexpected."
            )
        }
        this.distributionColumn.addAll(Arrays.asList(*columns))
        return this
    }

    fun createDistributedTableQuery(): String {
        val schemaQuery = super.createTableQuery()

        return "$schemaQuery; SELECT create_distributed_table('${distributionColumn.name}', colocate_with => $colocationColumn.name "
    }
}