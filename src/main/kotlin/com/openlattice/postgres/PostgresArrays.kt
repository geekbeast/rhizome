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
package com.openlattice.postgres

import kotlin.Throws
import java.sql.SQLException
import java.util.UUID
import com.openlattice.postgres.PostgresDatatype
import java.sql.Connection
import java.sql.ResultSet
import java.util.stream.Stream

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
object PostgresArrays {
    @JvmStatic
    @Throws(SQLException::class)
    fun createUuidArrayOfArrays(connection: Connection, idArrays: Stream<Array<UUID>>): java.sql.Array {
        return connection.createArrayOf(
                PostgresDatatype.UUID.sql(), idArrays.toArray()
        )
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createUuidArray(connection: Connection, ids: Stream<UUID>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.UUID.sql(), ids.toArray())
    }

    /**
     * Creates a Postgres UUID array from a collection of ids. This method is preferred for performance reasons to the
     * stream methods above.
     *
     * @param connection The JDBC connection to the database.
     * @param ids The ids for the array.
     * @return A postgres array of the ids in the iteration order of the ids collection.
     * @throws SQLException If something goes wrong.
     */
    @JvmStatic
    @Throws(SQLException::class)
    fun createUuidArray(connection: Connection, ids: Collection<UUID>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.UUID.sql(), ids.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createUuidArray(connection: Connection, vararg id: UUID): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.UUID.sql(), id)
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createLongArray(connection: Connection, values: Collection<Long>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.BIGINT.sql(), values.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createIntArray(connection: Connection, vararg value: Int): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.INTEGER.sql(), value.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createIntArray(connection: Connection, values: Collection<Int>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.INTEGER.sql(), values.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createLongArray(connection: Connection, vararg value: Long): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.BIGINT.sql(), value.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createTextArray(connection: Connection, ids: Stream<String>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.TEXT.sql(), ids.toArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createTextArray(connection: Connection, ids: Collection<String>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.TEXT.sql(), ids.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createTextArray(connection: Connection, vararg text: String): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.TEXT.sql(), text)
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createBooleanArray(connection: Connection, values: Collection<Boolean>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.BOOLEAN.sql(), values.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createShortArray(connection: Connection, values: Collection<Short>): java.sql.Array {
        return connection.createArrayOf(PostgresDatatype.SMALLINT.sql(), values.toTypedArray())
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun getTextArray(rs: ResultSet, column: String): Array<String> {
        return rs.getArray(column).array as Array<String>
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun getIntArray(rs: ResultSet, column: String): Array<Int> {
        return rs.getArray(column).array as Array<Int>
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun getLongArray(rs: ResultSet, column: String): Array<Long> {
        return rs.getArray(column).array as Array<Long>
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun getUuidArrayOfArrays(rs: ResultSet, column: String): Array<Array<UUID>> {
        return rs.getArray(column).array as Array<Array<UUID>>
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun getUuidArray(rs: ResultSet, column: String): Array<UUID>? {
        val arr = rs.getArray(column)
        return if (arr == null) {
            null
        } else {
            rs.getArray(column).array as Array<UUID>
        }
    }

}