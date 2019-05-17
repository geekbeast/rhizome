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

package com.openlattice.postgres.streams;

import com.google.common.collect.ImmutableList;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.xml.transform.Result;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class StatementHolder implements Closeable {
    private final Connection      connection;
    private final Statement       statement;
    private final ResultSet       resultSet;
    private final List<Statement> otherStatements;
    private final List<ResultSet> otherResultSets;
    private       boolean         open = true;

    public StatementHolder( Connection connection, Statement statement, ResultSet resultSet ) {
        this( connection, statement, resultSet, ImmutableList.of(), ImmutableList.of() );
    }

    public StatementHolder(
            Connection connection,
            Statement statement,
            ResultSet resultSet,
            List<Statement> otherStatements,
            List<ResultSet> otherResultSets ) {
        this.connection = connection;
        this.statement = statement;
        this.resultSet = resultSet;
        this.otherStatements = otherStatements;
        this.otherResultSets = otherResultSets;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override public void close() throws IOException {
        try {
            for ( ResultSet rs : otherResultSets ) {
                rs.close();
            }

            for ( Statement s : otherStatements ) {
                s.close();
            }

            resultSet.close();
            statement.close();
            connection.close();
            open = false;
        } catch ( SQLException e ) {
            throw new IOException( "Unable to close sql objects", e );
        }
    }

    public boolean isOpen() {
        return open;
    }
}
