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

import static com.google.common.base.Preconditions.checkState;

import com.dataloom.streams.StreamUtil;
import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresIterable<T> implements Iterable<T> {
    private static final Map<Class<?>, Logger> subclassLoggers = new ConcurrentHashMap<>();

    private final Logger logger = subclassLoggers.computeIfAbsent( getClass(), LoggerFactory::getLogger );

    private final Supplier<StatementHolder> rsh;
    private final Function<ResultSet, T>    mapper;

    public PostgresIterable(
            Supplier<StatementHolder> rsh,
            Function<ResultSet, T> mapper ) {
        this.rsh = rsh;
        this.mapper = mapper;
    }

    @Override
    public PostgresIterator<T> iterator() {
        try {
            return new PostgresIterator<>( rsh.get(), mapper );
        } catch ( SQLException e ) {
            logger.error( "Error creating postgres stream iterator." );
            throw new IllegalStateException( "Unable to instantiate postgres iterator.", e );
        }
    }

    @Nonnull
    public Stream<T> stream() {
        return StreamUtil.stream( this );
    }

    public static class PostgresIterator<T> implements Iterator<T>, AutoCloseable, Closeable {
        private static final long                   DEFAULT_TIMEOUT_MILLIS = 600000;
        private static final ExecutorService        executor               = Executors.newSingleThreadExecutor();
        private static final Logger                 logger                 = LoggerFactory
                .getLogger( PostgresIterator.class );
        private final        Lock                   lock                   = new ReentrantLock();
        private final        Function<ResultSet, T> mapper;
        private final        StatementHolder        rsh;
        private final        ResultSet              rs;
        private final        long                   timeoutMillis;
        private              long                   expiration;
        private              boolean                notExhausted;

        public PostgresIterator( StatementHolder rsh, Function<ResultSet, T> mapper ) throws SQLException {
            this( rsh, mapper, DEFAULT_TIMEOUT_MILLIS );
        }

        public PostgresIterator( StatementHolder rsh, Function<ResultSet, T> mapper, long timeoutMillis )
                throws SQLException {
            this.rsh = rsh;
            this.mapper = mapper;
            this.rs = rsh.getResultSet();
            this.timeoutMillis = timeoutMillis;
            notExhausted = this.rs.next();
            updateExpiration();

            if ( !notExhausted ) {
                rsh.close();
            }

            executor.execute( () -> {
                while ( rsh.isOpen() ) {
                    if ( System.currentTimeMillis() > expiration || !notExhausted ) {
                        rsh.close();
                    } else {
                        try {
                            Thread.sleep( timeoutMillis );
                        } catch ( InterruptedException e ) {
                            logger.error( "Unable to sleep thread for {} millis", timeoutMillis, e );
                        }
                    }
                }
            } );
        }

        @Override
        public boolean hasNext() {
            //We don't lock here, because multiple calls to has next can still cause an exception to be thrown while
            //calling next
            updateExpiration();
            return notExhausted;
        }

        private void updateExpiration() {
            this.expiration = System.currentTimeMillis() + timeoutMillis;
        }

        @Override
        public T next() {
            updateExpiration();
            final T nextElem;
            try {
                lock.lock();
                checkState( hasNext(), "There are no more items remaining in the stream." );
                nextElem = mapper.apply( rs );
                notExhausted = rs.next();
            } catch ( SQLException e ) {
                logger.error( "Unable to retrieve next element from result set.", e );
                notExhausted = false;
                throw new NoSuchElementException( "Unable to retrieve next element from result set." );
            } finally {
                if ( !notExhausted ) {
                    rsh.close();
                }

                lock.unlock();
            }

            return nextElem;
        }

        @Override public void close() {
            rsh.close();
        }
    }

}
