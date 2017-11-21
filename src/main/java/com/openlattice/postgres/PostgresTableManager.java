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
    private static final Logger logger = LoggerFactory.getLogger( PostgresTableManager.class );
    private final HikariDataSource hds;
    private final Map<String, PostgresTableDefinition> activeTables = new HashMap<>();

    public PostgresTableManager( HikariDataSource hds ) {
        this.hds = hds;
    }

    public void registerTables( PostgresTableDefinition... tables ) throws SQLException {
        registerTables( Arrays.asList( tables ) );
    }

    public void registerTables( Iterable<PostgresTableDefinition> tables ) throws SQLException {
        for ( PostgresTableDefinition table : tables ) {
            if ( activeTables.containsKey( table.getName() ) ) {
                logger.warn( "Table {} has already been registered and initialized... skipping", table );
            } else {
                try ( Connection conn = hds.getConnection(); Statement sctq = conn.createStatement() ) {
                    sctq.execute( table.createTableQuery() );
                    for ( PostgresIndexDefinition index : table.getIndexes() ) {
                        String indexSql = index.sql();
                        try( Statement sci = conn.createStatement() ) {
                            sci.execute( indexSql );
                        } catch ( SQLException e ) {
                            logger.info( "Failed to create index {} with query {} for table {}",
                                    index,
                                    indexSql,
                                    table );
                            throw e;
                        }
                    }
                } catch ( SQLException e ) {
                    logger.info( "Failed to initialize postgres table {} with query {}", table,table.createTableQuery(), e );
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
