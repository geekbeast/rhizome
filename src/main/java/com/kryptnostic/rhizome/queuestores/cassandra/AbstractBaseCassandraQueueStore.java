/*
 * Copyright (C) 2017. Kryptnostic, Inc (dba Loom)
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
 * You can contact the owner of the copyright at support@thedataloom.com
 */

package com.kryptnostic.rhizome.queuestores.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.QueueStoreConfig;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;
import com.kryptnostic.rhizome.queuestores.TestableSelfRegisteringQueueStore;

public abstract class AbstractBaseCassandraQueueStore<T> implements TestableSelfRegisteringQueueStore<T> {

    private static final Logger logger = LoggerFactory.getLogger( AbstractBaseCassandraQueueStore.class );

    private final   String                queueName;
    protected final Session               session;
    protected final CassandraTableBuilder cassandraTableBuilder;

    private final PreparedStatement deleteQuery;
    private final PreparedStatement loadQuery;
    private final PreparedStatement storeQuery;

    public AbstractBaseCassandraQueueStore( String queueName, Session session, CassandraTableBuilder builder ) {

        Preconditions.checkArgument( StringUtils.isNotBlank( queueName ), "Queue name cannot be blank" );
        this.queueName = queueName;
        this.session = Preconditions.checkNotNull( session, "Session cannot be null" );
        this.cassandraTableBuilder = Preconditions.checkNotNull( builder, "CassandraTableBuilder cannot be null" );

        deleteQuery = session.prepare( builder.buildDeleteQuery() );
        loadQuery = session.prepare( builder.buildLoadQuery() );
        storeQuery = session.prepare( builder.buildStoreQuery() );
    }

    @Override
    public QueueConfig getQueueConfig() {

        return new QueueConfig( queueName )
                .setBackupCount( cassandraTableBuilder.getReplicationFactor() )
                .setQueueStoreConfig( getQueueStoreConfig() );
    }

    @Override
    public QueueStoreConfig getQueueStoreConfig() {

        return new QueueStoreConfig()
                .setEnabled( true )
                .setStoreImplementation( this );
    }

    @Override
    public String getQueueName() {

        return queueName;
    }

    @Override
    public String getTable() {

        return cassandraTableBuilder.getName();
    }

    @Override
    public T generateTestValue() {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED! Override this method in your subclass." );
    }

    @Override
    public void delete( Long key ) {

        asyncDelete( key ).getUninterruptibly();
    }

    @Override
    public void deleteAll( Collection<Long> keys ) {

        keys
                .parallelStream()
                .map( this::asyncDelete )
                .forEach( ResultSetFuture::getUninterruptibly );
    }

    @Override
    public T load( Long key ) {

        return safeTransform( asyncLoad( key ) );
    }

    @Override
    public Map<Long, T> loadAll( Collection<Long> keys ) {

        return keys
                .stream()
                .map( key -> Pair.of( key, asyncLoad( key ) ) )
                .map( pair -> Pair.of( pair.getLeft(), safeTransform( pair.getRight() ) ) )
                .filter( pair -> pair.getRight() != null )
                .collect( Collectors.toMap( Pair::getLeft, Pair::getRight ) );
    }

    @Override
    public Set<Long> loadAllKeys() {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public void store( Long key, T value ) {

        asyncStore( key, value ).getUninterruptibly();
    }

    @Override
    public void storeAll( Map<Long, T> map ) {

        map
                .entrySet()
                .parallelStream()
                .map( entry -> asyncStore( entry.getKey(), entry.getValue() ) )
                .forEach( ResultSetFuture::getUninterruptibly );
    }

    protected ResultSetFuture asyncDelete( Long key ) {

        try {
            return session.executeAsync( bind( key, deleteQuery.bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to delete key {}", key, e );
            return null;
        }
    }

    protected ResultSetFuture asyncLoad( Long key ) {

        try {
            return session.executeAsync( bind( key, loadQuery.bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to load key {}", key, e );
            return null;
        }
    }

    protected ResultSetFuture asyncStore( Long key, T value ) {
        try {
            return session.executeAsync( bind( key, value, storeQuery.bind() ) );
        } catch ( NoHostAvailableException | QueryExecutionException | QueryValidationException e ) {
            logger.error( "Unable to perform query to store key {}", key, e );
            return null;
        }
    }

    protected T safeTransform( ResultSetFuture resultSetFuture ) {

        ResultSet resultSet = resultSetFuture.getUninterruptibly();
        return resultSet == null
                ? null
                : mapValue( resultSet );
    }

    protected abstract BoundStatement bind( Long key, BoundStatement bs );

    protected abstract BoundStatement bind( Long key, T value, BoundStatement bs );

    protected abstract T mapValue( ResultSet resultSet );
}
