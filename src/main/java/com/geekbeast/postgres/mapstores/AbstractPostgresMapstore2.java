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

import static com.google.common.base.Preconditions.checkState;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.geekbeast.rhizome.mapstores.TestableSelfRegisteringMapStore;
import com.geekbeast.postgres.PostgresColumnDefinition;
import com.geekbeast.postgres.PostgresTableDefinition;
import com.geekbeast.postgres.streams.BasePostgresIterable;
import com.geekbeast.postgres.streams.StatementHolderSupplier;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public abstract class AbstractPostgresMapstore2<K, V> implements TestableSelfRegisteringMapStore<K, V> {
    public static final int BATCH_SIZE = 1 << 12;

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    protected final PostgresTableDefinition table;
    protected final HikariDataSource        hds;
    protected final int                     batchSize;
    protected final int                     batchCapacity;
    protected final String                  mapName;
    private final   String                  insertQuery;
    private final   String                  deleteQuery;
    private final   String                  selectAllKeysQuery;
    private final   String                  selectByKeyQuery;
    private final   String                  selectInQuery;

    private final Optional<String> oc;

    private final List<PostgresColumnDefinition> keyColumns;
    private final List<PostgresColumnDefinition> valueColumns;

    private final MapStoreConfig mapStoreConfig = new MapStoreConfig()
            .setInitialLoadMode( MapStoreConfig.InitialLoadMode.EAGER )
            .setImplementation( this )
            .setEnabled( true )
            .setWriteDelaySeconds( 0 );

    public AbstractPostgresMapstore2(
            String mapName,
            PostgresTableDefinition table,
            HikariDataSource hds,
            int batchSize ) {
        this.table = table;
        this.hds = hds;
        this.mapName = mapName;
        this.batchSize = batchSize;
        initMapstore();
        this.keyColumns = initKeyColumns();
        this.valueColumns = initValueColumns();
        this.batchCapacity = batchSize * getSelectInParameterCount();
        checkState( batchCapacity < ( 1 << 16 ),
                "The selected batch size results in too large of batch capacity for Postgres (limit 65536 arguments for in statement" );

        this.oc = buildOnConflictQuery();

        this.insertQuery = buildInsertQuery();
        this.deleteQuery = buildDeleteQuery();
        this.selectAllKeysQuery = buildSelectAllKeysQuery();
        this.selectByKeyQuery = buildSelectByKeyQuery();
        this.selectInQuery = buildSelectInQuery();
    }

    protected void initMapstore() {}

    protected final List<PostgresColumnDefinition> keyColumns() {
        return keyColumns;
    }

    protected List<PostgresColumnDefinition> initKeyColumns() {
        return ImmutableList.copyOf( table.getPrimaryKey() );
    }

    protected final List<PostgresColumnDefinition> valueColumns() {
        return valueColumns;
    }

    protected List<PostgresColumnDefinition> initValueColumns() {
        return ImmutableList.copyOf( Sets.difference( table.getColumns(), table.getPrimaryKey() ) );
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

    protected String buildSelectInQuery() {
        return table.selectInQuery( ImmutableList.of(), keyColumns(), batchSize );
    }

    protected int getSelectInParameterCount() {
        return keyColumns().size();
    }

    @Timed
    @Override
    public void delete( K key ) {
        try ( Connection connection = hds.getConnection(); PreparedStatement deleteRow = prepareDelete( connection ) ) {
            bind( deleteRow, key );
            deleteRow.executeUpdate();
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during delete for key {} in map {}.", key, mapName, e );
        }
    }

    @Timed
    @Override
    public void deleteAll( Collection<K> keys ) {
        K key = null;
        try ( Connection connection = hds.getConnection(); PreparedStatement deleteRow = prepareDelete( connection ) ) {
            for ( K k : keys ) {
                key = k;
                bind( deleteRow, key );
                deleteRow.addBatch();
            }
            deleteRow.executeBatch();
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during delete all for key {} in map {}", key, mapName, e );
        }
    }

    protected V readNext( ResultSet rs ) throws SQLException {
        V val = mapToValue( rs );
        logger.debug( "LOADED value {} in map {}", val, mapName );
        return val;
    }

    protected V loadUsing( K key, Connection connection ) {
        try ( PreparedStatement selectRow = prepareSelectByKey( connection ) ) {
            bind( selectRow, key );
            try ( ResultSet rs = selectRow.executeQuery() ) {
                if ( rs.next() ) {
                    return readNext( rs );
                }
            } catch ( SQLException e ) {
                final String errMsg = "Error executing SQL during select for key " + key + ".";
                logger.error( errMsg, e );
                throw new IllegalStateException( errMsg, e );
            }
        } catch ( SQLException e ) {
            final String errMsg = "Error binding to select for key " + key + " in map " + mapName + ".";
            logger.error( errMsg, key, e );
            throw new IllegalArgumentException( errMsg, e );
        }
        return null;
    }

    @Timed
    @Override
    public V load( K key ) {
        try ( Connection connection = hds.getConnection() ) {
            return loadUsing( key, connection );
        } catch ( SQLException e ) {
            final String errMsg = "Unable to connect to database to load key " + key.toString() +
                    " map +" + mapName + "!";
            logger.error( errMsg, key, e );
            throw new IllegalStateException( errMsg, e );
        }
    }

    @Timed
    @Override
    public Map<K, V> loadAll( Collection<K> keys ) {
        Map<K, V> result = Maps.newLinkedHashMapWithExpectedSize(keys.size());

        K key = null;
        try ( Connection connection = hds.getConnection();
                                PreparedStatement selectWhere = prepareSelectByKey( connection ) ) {
                for(var k : keys ) {
                    key = k;
                    bind(selectWhere, k,1);
                    var rs = selectWhere.executeQuery();
                    if(rs.next()) {
                        result.put( k, mapToValue(rs) );
                    }
                }
        } catch ( SQLException e ) {
            logger.error( "Error executing SQL during select for key {} in map {}.", key, mapName, e );
        }
        return result;
    }

    @Override public Iterable<K> loadAllKeys() {
        logger.info( "Starting load all keys for map {}", mapName );
        return new BasePostgresIterable<>(
                new StatementHolderSupplier( hds, selectAllKeysQuery, 50_000, false, 0L),
                rs -> {
                    try {
                        return mapToKey( rs );
                    } catch ( SQLException e ) {
                        logger.error( "Unable to read keys for map {}.", mapName, e);
                        throw new IllegalStateException( "Unable to read keys.", e );
                    }
                }
        );
    }

    @Override
    public MapStoreConfig getMapStoreConfig() {
        return mapStoreConfig;
    }

    @Override
    public MapConfig getMapConfig() {
        return new MapConfig( getMapName() )
                .setMapStoreConfig( getMapStoreConfig() );
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
        //TODO: We should make insert columns equal key columns + value columns explicitly.
        //An empty list of columns means all
        List<PostgresColumnDefinition> insertColumns = new ArrayList<>( keyColumns().size() + valueColumns().size() );
        insertColumns.addAll( keyColumns() );
        insertColumns.addAll( valueColumns() );

        return insertColumns;
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

    protected PreparedStatement prepareSelectIn( Connection connection ) throws SQLException {
        return connection.prepareStatement( selectInQuery );
    }

    protected String selectByKeyQuery() {
        return selectByKeyQuery;
    }

    protected PreparedStatement prepareSelectByKey( Connection connection ) throws SQLException {
        return connection.prepareStatement( selectByKeyQuery );
    }

    protected void handleStoreFailed( K key, V value ) {
        //Do nothing by default
    }

    protected void handleStoreSucceeded( K key, V value ) {
    }

    //protected void handleStoreAllEntrySucceeded( K key, V value ) {}

    protected void handleStoreAllSucceeded( Map<K, V> m ) {
    }

    protected abstract int bind( PreparedStatement ps, K key, int offset ) throws SQLException;

    protected int bind( PreparedStatement ps, K key ) throws SQLException {
        return bind( ps, key, 1 );
    }

    protected abstract K mapToKey( ResultSet rs ) throws SQLException;

    protected abstract V mapToValue( ResultSet rs ) throws SQLException;
}
