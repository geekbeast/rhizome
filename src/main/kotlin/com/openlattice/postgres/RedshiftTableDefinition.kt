package com.openlattice.postgres

import org.apache.commons.lang3.RandomStringUtils

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class RedshiftTableDefinition(name: String) : PostgresTableDefinition(name) {
    private val sortKey: MutableList<PostgresColumnDefinition> = mutableListOf()

    fun sortKey(vararg sortKey : PostgresColumnDefinition): RedshiftTableDefinition {
        this.sortKey.addAll(sortKey.asList())
        return this
    }

    fun createTempTable(suffix: String = RandomStringUtils.randomAlphanumeric(10) ) : RedshiftTableDefinition {
        return RedshiftTableDefinition("${this.name}_$suffix")
                .temporary()
                .sortKey(*sortKey.toTypedArray())
                .addColumns(*this.columns.toTypedArray())
    }

    override fun temporary(): RedshiftTableDefinition {
        super.temporary()
        return this
    }

    override fun addDataSourceNames(vararg datasources: String?): RedshiftTableDefinition {
        super.addDataSourceNames(*datasources)
        return this
    }

    override fun addColumns(vararg columnsToAdd: PostgresColumnDefinition?): RedshiftTableDefinition {
        super.addColumns(*columnsToAdd)
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