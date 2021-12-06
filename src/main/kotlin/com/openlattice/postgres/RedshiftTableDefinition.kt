package com.openlattice.postgres

import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class RedshiftTableDefinition(name: String) : PostgresTableDefinition(name) {
    private val sortKey: MutableList<PostgresColumnDefinition> = mutableListOf()

    fun sortKey(sortKey: List<PostgresColumnDefinition>): RedshiftTableDefinition {
        this.sortKey.addAll(sortKey)
        return this
    }

    override fun createTableQuery(): String {
        validate()
        val ctb = StringBuilder("CREATE ")

        if (temporary) {
            ctb.append("TEMPORARY ")
        }

        if (unlogged) {
            ctb.append("UNLOGGED ")
        }

        ctb.append("TABLE ")

        if (ifNotExists) {
            ctb.append(" IF NOT EXISTS ")
        }

        val columnSql = columns.joinToString(",") { it.sql() }

        ctb.append(name).append(" (").append(columnSql)

        if (primaryKey.isNotEmpty()) {
            val pkSql = primaryKey.joinToString(", ") { it.name }
            ctb.append(", PRIMARY KEY (").append(pkSql).append(" )")
        }

        if (unique.isNotEmpty()) {
            val uSql = unique.joinToString(",") { it.name }
            ctb.append(", UNIQUE (").append(uSql).append(" )")
        }

        ctb.append(") ")

        ctb
            .append(" SORTKEY (")
            .append(sortKey.joinToString(",") { it.name })
            .append(")")

        return ctb.toString()
    }
}