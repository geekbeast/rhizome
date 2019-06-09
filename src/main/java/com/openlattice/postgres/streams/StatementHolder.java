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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class StatementHolder implements Closeable {
    private static final Logger logger                          = LoggerFactory.getLogger( StatementHolder.class );
    private static final long   LONG_RUNNING_QUERY_LIMIT_MILLIS = 15000;

    private final Connection      connection;
    private final Statement       statement;
    private final ResultSet       resultSet;
    private final List<Statement> otherStatements;
    private final List<ResultSet> otherResultSets;
    private final Stopwatch       sw   = Stopwatch.createStarted();
    private final long            longRunningQueryLimit;
    private       boolean         open = true;

    public StatementHolder( Connection connection, Statement statement, ResultSet resultSet ) {
        this( connection,
                statement,
                resultSet,
                ImmutableList.of(),
                ImmutableList.of(),
                LONG_RUNNING_QUERY_LIMIT_MILLIS );
    }

    public StatementHolder(
            Connection connection,
            Statement statement,
            ResultSet resultSet,
            long longRunningQueryLimit ) {
        this( connection, statement, resultSet, ImmutableList.of(), ImmutableList.of(), longRunningQueryLimit );
    }

    public StatementHolder(
            Connection connection,
            Statement statement,
            ResultSet resultSet,
            List<Statement> otherStatements,
            List<ResultSet> otherResultSets,
            long longRunningQueryLimit ) {
        this.connection = connection;
        this.statement = statement;
        this.resultSet = resultSet;
        this.otherStatements = otherStatements;
        this.otherResultSets = otherResultSets;
        this.longRunningQueryLimit = longRunningQueryLimit;
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

    @Override
    public void close() {
        otherResultSets.forEach( this::safeTryClose );
        otherStatements.forEach( this::safeTryClose );

        final var elapsed = sw.elapsed( TimeUnit.MILLISECONDS );
        if ( elapsed > this.longRunningQueryLimit ) {
            logger.warn( "The following SQL query took {} ms: {}", elapsed, statement.toString() );
        }

        sw.stop();
        safeTryClose( resultSet );
        safeTryClose( statement );
        safeTryClose( connection );

        open = false;
    }

    private void safeTryClose( AutoCloseable obj ) {
        try {
            obj.close();
        } catch ( Exception e ) {
            logger.error( "Unable to close obj {}", obj, e );
        }
    }

    public boolean isOpen() {
        return open;
    }
}
