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

package com.openlattice.postgres.mapstores;

import com.dataloom.streams.StreamUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.kryptnostic.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.openlattice.postgres.CountdownConnectionCloser;
import com.openlattice.postgres.KeyIterator;
import com.openlattice.postgres.PostgresColumnDefinition;
import com.openlattice.postgres.PostgresTableDefinition;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public abstract class AbstractBasePostgresMapstore<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    protected final PostgresTableDefinition table;
    protected final Logger logger = LoggerFactory.getLogger( getClass() );
    protected final HikariDataSource hds;
    private final   String           mapName;
    private final   String           insertQuery;
    private final   String           deleteQuery;
    private final   String           selectAllKeysQuery;
    private final   String           selectByKeyQuery;
    private final   Optional<String> oc;

    public AbstractBasePostgresMapstore( String mapName, PostgresTableDefinition table, HikariDataSource hds ) {
        this.mapName = mapName;
        this.table = table;
        this.hds = hds;

        this.oc = buildOnConflictQuery();

        this.insertQuery = buildInsertQuery();
        this.deleteQuery = buildDeleteQuery();
        this.selectAllKeysQuery = buildSelectAllKeysQuery();
        this.selectByKeyQuery = buildSelectByKeyQuery();
    }

    protected Optional<String> buildOnConflictQuery() {
        return Optional.of( ( " ON CONFLICT ("
                + keyColumns().stream()
                .map( PostgresColumnDefinition::getName )
                .collect( Collectors.joining( ", " ) )
                + ") DO "
                + table.updateQuery( keyColumns(), valueColumns(), false ) ) );
    }

    protected String buildInsertQuery() {
        return table.insertQuery( onConflict(), getInsertColumns() );
    }

    protected String buildDeleteQuery() {
        return table.deleteQuery( keyColumns() );
    }

    protected String buildSelectAllKeysQuery() {
        return table.selectQuery( keyColumns() );
    }

    protected String buildSelectByKeyQuery() {
        return table.selectQuery( ImmutableList.of(), keyColumns() );
    }

    @Override
    public void store( K key, V value ) {
        try ( Connection connection = hds.getConnection(); PreparedStatement insertRow = prepareInsert( connection ) ) {
            bind( insertRow, key, value );
            logger.debug( "Insert query: {}", insertRow );
            insertRow.execute();
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during store for key {}.", key, e );
            handleStoreFailed( key, value );
        }
    }

    @Override
    public void storeAll( Map<K, V> map ) {
        K key = null;

        try ( Connection connection = hds.getConnection();
                PreparedStatement insertRow = prepareInsert( connection ) ) {

            for ( Entry<K, V> entry : map.entrySet() ) {
                key = entry.getKey();
                bind( insertRow, key, entry.getValue() );
                insertRow.addBatch();
            }

            insertRow.executeBatch();
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during store all for key {}", key, e );
        }
    }

    @Override public void delete( K key ) {
        try ( Connection connection = hds.getConnection(); PreparedStatement deleteRow = prepareDelete( connection ) ) {
            bind( deleteRow, key );
            deleteRow.executeUpdate();
            connection.close();
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during delete for key {}.", key, e );
        }
    }

    @Override public void deleteAll( Collection<K> keys ) {
        K key = null;
        try ( Connection connection = hds.getConnection(); PreparedStatement deleteRow = prepareDelete( connection ) ) {

            connection.setAutoCommit( false );
            for ( K k : keys ) {
                key = k;
                bind( deleteRow, key );
                deleteRow.addBatch();
            }

            deleteRow.executeBatch();

            connection.commit();
            connection.setAutoCommit( true );
            connection.close();
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during delete all for key {}", key, e );
        }
    }

    @Override
    public V load( K key ) {
        V val = null;
        try ( Connection connection = hds.getConnection();
                PreparedStatement selectRow = prepareSelectByKey( connection ) ) {
            bind( selectRow, key );
            ResultSet rs = selectRow.executeQuery();
            if ( rs.next() ) {
                val = mapToValue( rs );
            }
            connection.close();
            logger.debug( "LOADED: {}", val );
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during select for key {}.", key, e );
        }
        return val;
    }

    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> result = new MapMaker().initialCapacity( keys.size() ).makeMap();
        keys.parallelStream().forEach( key -> {
            V value = load( key );
            if ( value != null ) { result.put( key, value ); }
        } );
        return result;
    }

    @Override public Iterable<K> loadAllKeys() {
        logger.info( "Starting load all keys for Edge Mapstore" );
        try {
            Connection connection = hds.getConnection();
            Statement stmt = connection.createStatement();
            connection.setAutoCommit( false );
            stmt.setFetchSize( 50000 );
            final ResultSet rs = stmt.executeQuery( selectAllKeysQuery );
            return StreamUtil
                    .stream( () -> new KeyIterator<K>( rs,
                            new CountdownConnectionCloser( connection, 1 ), this::mapToKey ) )
                    .peek( key -> logger.debug( "Key to load: {}", key ) )
                    ::iterator;
        } catch ( SQLException e ) {
            logger.error( "Unable to acquire connection load all keys" );
            return null;
        }
    }

    @Override public String getMapName() {
        return mapName;
    }

    @Override public String getTable() {
        return table.getName();
    }

    protected Optional<String> onConflict() {
        return oc;
    }

    protected List<PostgresColumnDefinition> getInsertColumns() {
        //An empty list of columns means all
        return ImmutableList.of();
    }

    protected String getInsertQuery() {
        return insertQuery;
    }

    protected PreparedStatement prepareInsert( Connection connection ) throws SQLException {
        return connection.prepareStatement( getInsertQuery() );
    }

    protected String getDeleteQuery() {
        return deleteQuery;
    }

    protected PreparedStatement prepareDelete( Connection connection ) throws SQLException {
        return connection.prepareStatement( deleteQuery );
    }

    protected String getSelecAllKeysQuery() {
        return selectAllKeysQuery;
    }

    protected PreparedStatement prepareSelectAllKeys( Connection connection ) throws SQLException {
        return connection.prepareStatement( selectAllKeysQuery );
    }

    protected String selectByKeyQuery() {
        return selectByKeyQuery;
    }

    protected PreparedStatement prepareSelectByKey( Connection connection ) throws SQLException {
        return connection.prepareStatement( selectByKeyQuery );
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return new MapStoreConfig()
                .setImplementation( this )
                .setEnabled( true )
                .setWriteDelaySeconds( 0 );
    }

    @Override
    public MapConfig getMapConfig() {
        return new MapConfig( getMapName() )
                .setMapStoreConfig( getMapStoreConfig() );
    }

    protected void handleStoreFailed( K key, V value ) {
        //Do nothing by default
    }

    protected List<PostgresColumnDefinition> keyColumns() {
        return ImmutableList.copyOf( table.getPrimaryKey() );
    }

    protected List<PostgresColumnDefinition> valueColumns() {
        return ImmutableList.copyOf( table.getColumns() );
    }

    /**
     * You must bind update parameters as well as insert parameters
     */
    protected abstract void bind( PreparedStatement ps, K key, V value ) throws SQLException;

    protected abstract void bind( PreparedStatement ps, K key ) throws SQLException;

    protected abstract V mapToValue( ResultSet rs ) throws SQLException;

    protected abstract K mapToKey( ResultSet rs ) throws SQLException;
}
