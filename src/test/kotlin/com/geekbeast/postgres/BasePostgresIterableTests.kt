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
package com.geekbeast.postgres

import com.geekbeast.postgres.streams.BasePostgresIterable
import com.geekbeast.postgres.streams.StatementHolder
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class BasePostgresIterableTests {
    @Test
    @Throws(SQLException::class)
    fun testCloseOnEmpty() {
        val closeCount = AtomicInteger()
        val rs = Mockito.mock(ResultSet::class.java)
        val stmt = Mockito.mock(Statement::class.java)
        val connection = Mockito.mock(Connection::class.java)
        Mockito.`when`(rs.next()).thenReturn(false)
        val pi: BasePostgresIterable<Any> = BasePostgresIterable(
                Supplier { StatementHolder(connection, stmt, rs) }
        ) { Any() }
        Mockito.doAnswer { closeCount.getAndIncrement() }.`when`(rs).close()
        Mockito.doAnswer { closeCount.getAndIncrement() }.`when`(stmt).close()
        Mockito.doAnswer { closeCount.getAndIncrement() }.`when`(connection).close()
        pi.iterator()
        Assert.assertEquals(3, closeCount.get().toLong())
    }
}