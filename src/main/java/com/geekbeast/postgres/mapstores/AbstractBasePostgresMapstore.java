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

package com.geekbeast.postgres.mapstores;

import com.codahale.metrics.annotation.Timed;
import com.geekbeast.postgres.PostgresTableDefinition;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public abstract class AbstractBasePostgresMapstore<K, V> extends AbstractPostgresMapstore2<K, V> {

    public AbstractBasePostgresMapstore(TypedMapIdentifier<K, V> identifier, PostgresTableDefinition table, HikariDataSource hds) {
        this( identifier.name(), table, hds );
    }

    public AbstractBasePostgresMapstore( String mapName, PostgresTableDefinition table, HikariDataSource hds ) {
        this( mapName, table, hds, BATCH_SIZE );
    }

    public AbstractBasePostgresMapstore(
            String mapName,
            PostgresTableDefinition table,
            HikariDataSource hds,
            int batchSize ) {
        super(mapName, table, hds, batchSize );
    }

    @Timed
    @Override
    public void store( K key, V value ) {
        try ( Connection connection = hds.getConnection(); PreparedStatement insertRow = prepareInsert( connection ) ) {
            bind( insertRow, key, value );
            logger.debug( "Insert query: {}", insertRow );
            insertRow.execute();
            handleStoreSucceeded( key, value );
        } catch ( SQLException e ) {
            String errMsg = "Error executing SQL during store for key " + key + " in map " + mapName + ".";
            logger.error( errMsg, e );
            handleStoreFailed( key, value );
            throw new IllegalStateException( errMsg, e );
        }

    }

    @Timed
    @Override
    public void storeAll( Map<K, V> map ) {
        K key = null;
        try ( Connection connection = hds.getConnection(); PreparedStatement insertRow = prepareInsert( connection ) ) {
            //TODO: We might want to do an inner try catch here to get specific logging on what failed
            for ( Entry<K, V> entry : map.entrySet() ) {
                key = entry.getKey();
                bind( insertRow, key, entry.getValue() );
                insertRow.addBatch();
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
    protected abstract void bind( PreparedStatement ps, K key, V value ) throws SQLException;

    protected abstract K mapToKey( ResultSet rs ) throws SQLException;
}
