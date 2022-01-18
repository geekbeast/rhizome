/*
 * Copyright (C) 2018. OpenLattice, Inc
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

package com.geekbeast.postgres.mapstores;

import com.codahale.metrics.annotation.Timed;
import com.geekbeast.postgres.PostgresTableDefinition;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public abstract class AbstractBaseSplitKeyPostgresMapstore<K, K2, V>
        extends AbstractPostgresMapstore2<K, Map<K2, V>> {

    public AbstractBaseSplitKeyPostgresMapstore( String mapName, PostgresTableDefinition table, HikariDataSource hds ) {
        this( mapName, table, hds, BATCH_SIZE );
    }

    //Hack for late initialization
    protected AbstractBaseSplitKeyPostgresMapstore( String mapName, PostgresTableDefinition table, HikariDataSource hds, Object lateinit ) {
        this( mapName, table, hds, BATCH_SIZE );
    }

    public AbstractBaseSplitKeyPostgresMapstore(
            String mapName,
            PostgresTableDefinition table,
            HikariDataSource hds,
            int batchSize ) {
        super( mapName, table, hds, batchSize );
    }

    @Timed
    @Override
    public void store( K key, Map<K2, V> value ) {
        try ( Connection connection = hds.getConnection(); PreparedStatement insertRow = prepareInsert( connection ) ) {
            for ( Entry<K2, V> entry : value.entrySet() ) {
                bind( insertRow, key, entry.getKey(), entry.getValue() );
                logger.debug( "Insert query: {}", insertRow );
                insertRow.addBatch();
            }
            insertRow.executeBatch();
            handleStoreSucceeded( key, value );
        } catch ( SQLException e ) {
            String errMsg = "Error executing SQL during store for key " + key + "in map " + mapName + ".";
            logger.error( errMsg, e );
            handleStoreFailed( key, value );
            throw new IllegalStateException( errMsg, e );
        }

    }

    @Timed
    @Override
    public void storeAll( Map<K, Map<K2, V>> map ) {
        K key = null;
        try ( Connection connection = hds.getConnection(); PreparedStatement insertRow = prepareInsert( connection ) ) {
            //TODO: We might want to do an inner try catch here to get specific logging on what failed
            for ( Entry<K, Map<K2, V>> entry : map.entrySet() ) {
                key = entry.getKey();
                for ( Entry<K2, V> subEntry : entry.getValue().entrySet() ) {
                    bind( insertRow, key, subEntry.getKey(), subEntry.getValue() );
                    insertRow.addBatch();
                }
            }
            insertRow.executeBatch();
            handleStoreAllSucceeded( map );
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during store all for key {} in map {}", key, mapName, e );
        }
    }

    /**
     * You must bind update parameters as well as insert parameters
     */
    protected abstract void bind( PreparedStatement ps, K key, K2 subKey, V value ) throws SQLException;

}
