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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.google.common.base.Preconditions;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.QueueStoreConfig;
import com.kryptnostic.rhizome.cassandra.CassandraTableBuilder;
import com.kryptnostic.rhizome.queuestores.TestableSelfRegisteringQueueStore;

public abstract class AbstractBaseCassandraQueueStore<T> implements TestableSelfRegisteringQueueStore<T> {

    private static final Logger logger = LoggerFactory.getLogger( AbstractBaseCassandraQueueStore.class );

    private final   String                queueName;
    protected final Session               session;
    protected final CassandraTableBuilder cassandraTableBuilder;

    public AbstractBaseCassandraQueueStore( String queueName, Session session, CassandraTableBuilder builder ) {

        Preconditions.checkArgument( StringUtils.isNotBlank( queueName ), "Queue name cannot be blank" );
        this.queueName = queueName;
        this.session = Preconditions.checkNotNull( session, "Session cannot be null" );
        this.cassandraTableBuilder = Preconditions.checkNotNull( builder, "CassandraTableBuilder cannot be null" );
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

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public void deleteAll( Collection<Long> keys ) {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public T load( Long key ) {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public Map<Long, T> loadAll( Collection<Long> keys ) {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public Set<Long> loadAllKeys() {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public void store( Long key, T value ) {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

    @Override
    public void storeAll( Map<Long, T> map ) {

        throw new UnsupportedOperationException( "NOT IMPLEMENTED!" );
    }

}
