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

package com.openlattice.postgres;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresTableManager {
    private static final Logger                               logger                                = LoggerFactory
            .getLogger( PostgresTableManager.class );

    private static final String                               INVALID_TABLE_DEFINITION_SQL_STATE    = "42P16";

    private final        HikariDataSource                     hds;
    private final        Map<String, PostgresTableDefinition> activeTables                          = new HashMap<>();
    private final        boolean                              citus;
    private final        boolean                              initializeIndices;

    public PostgresTableManager( HikariDataSource hds ) {
        this( hds, false, false );
    }

    public PostgresTableManager( HikariDataSource hds, boolean citus, boolean initializeIndices ) {
        this.citus = citus;
        this.hds = hds;
        this.initializeIndices = initializeIndices;
    }

    public void registerTables( PostgresTableDefinition... tables ) throws SQLException {
        registerTables( Arrays.asList( tables ) );
    }

    public void registerTables( Iterable<PostgresTableDefinition> tables ) throws SQLException {
        logger.info( "Processing postgres table registrations." );
        for ( PostgresTableDefinition table : tables ) {
            if ( activeTables.containsKey( table.getName() ) ) {
                logger.debug( "Table {} has already been registered and initialized... skipping", table );
            } else {
                logger.debug( "Processed postgres table registration for table {}", table.getName() );
                try ( Connection conn = hds.getConnection(); Statement sctq = conn.createStatement() ) {
                    sctq.execute( table.createTableQuery() );

                    if ( citus ) {
                        //Creating the distributed table must be done before creating any indices.
                        if ( table instanceof CitusDistributedTableDefinition ) {
                            logger.info( "Creating distributed table {}.", table.getName() );
                            try ( Statement ddstmt = conn.createStatement() ) {
                                ddstmt.execute( ( (CitusDistributedTableDefinition) table )
                                        .createDistributedTableQuery() );
                            } catch ( SQLException ddex ) {
                                if ( ddex.getSQLState().equals( INVALID_TABLE_DEFINITION_SQL_STATE )
                                        && ddex.getMessage()
                                        .contains( "table \"" + table.getName() + "\" is already distributed" ) ) {
                                    logger.info( "Table {} is already distributed.", table.getName() );
                                } else {
                                    logger.error( "Unable to distribute table {}. Cause: {}",
                                            table.getName(),
                                            ddex.getMessage(),
                                            ddex );
                                }
                            }
                        }
                    }

                    if ( initializeIndices ) {
                        for ( PostgresIndexDefinition index : table.getIndexes() ) {
                            String indexSql = index.sql();
                            try ( Statement sci = conn.createStatement() ) {
                                sci.execute( indexSql );
                            } catch ( SQLException e ) {
                                logger.info( "Failed to create index {} with query {} for table {}",
                                        index,
                                        indexSql,
                                        table );
                                throw e;
                            }
                        }
                    }
                } catch ( SQLException e ) {
                    logger.info( "Failed to initialize postgres table {} with query {}",
                            table,
                            table.createTableQuery(),
                            e );
                    throw e;
                }

            }
            activeTables.put( table.getName(), table );
        }
    }

    public HikariDataSource getHikariDataSource() {
        return hds;
    }

    public PostgresTableDefinition getTable( String name ) {
        return activeTables.get( name );
    }

}
