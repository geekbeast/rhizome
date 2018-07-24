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

import static org.mockito.Mockito.doAnswer;

import com.openlattice.postgres.streams.PostgresIterable;
import com.openlattice.postgres.streams.StatementHolder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresIterableTests {
    @Test
    public void testCloseOnEmpty() throws SQLException {
        final AtomicInteger closeCount = new AtomicInteger();
        var rs = Mockito.mock( ResultSet.class );
        var stmt = Mockito.mock( Statement.class );
        var connection = Mockito.mock( Connection.class );

        Mockito.when( rs.next() ).thenReturn( false );
        var pi = new PostgresIterable<Object>(
                () -> new StatementHolder( connection, stmt, rs ),
                resultSet -> new Object()
        );

        doAnswer( invocation -> closeCount.getAndIncrement() ).when( stmt ).close();
        doAnswer( invocation -> closeCount.getAndIncrement() ).when( connection ).close();

        Assert.assertEquals( 3, closeCount.get());
    }

}
