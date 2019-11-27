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
        name: String
) : PostgresTableDefinition(name) {
    private lateinit var distributionColumn: PostgresColumnDefinition
    private var colocationColumn: Optional<PostgresTableDefinition> = Optional.empty()
    private var unlogged = false

    fun colocationColumn(column: PostgresTableDefinition): CitusDistributedTableDefinition {
        this.colocationColumn = Optional.of(column)
        return this
    }

    fun distributionColumn(column: PostgresColumnDefinition): CitusDistributedTableDefinition {
        this.distributionColumn = column
        return this
    }

    fun createDistributedTableQuery(): String {
        return if( colocationColumn.isPresent ) {
            val colocationSql = colocationColumn.map { "colocate_with =>'${it.name}'" }.get()
            "SELECT create_distributed_table('$name','${distributionColumn.name}', $colocationSql)"
        } else {
            "SELECT create_distributed_table('$name','${distributionColumn.name}')"
        }
    }

    override fun unlogged(): CitusDistributedTableDefinition {
        super.unlogged()
        return this
    }

    override fun addColumns(vararg columnsToAdd: PostgresColumnDefinition): CitusDistributedTableDefinition {
        super.addColumns(*columnsToAdd)
        return this
    }

    override fun addIndexes(vararg indexes: PostgresIndexDefinition): CitusDistributedTableDefinition {
        super.addIndexes(*indexes)
        return this
    }

    override fun primaryKey(vararg primaryKeyColumns: PostgresColumnDefinition): CitusDistributedTableDefinition {
        super.primaryKey(*primaryKeyColumns)
        return this
    }

    override fun setUnique(vararg uniqueColumns: PostgresColumnDefinition): CitusDistributedTableDefinition {
        super.setUnique(*uniqueColumns)
        return this
    }
}