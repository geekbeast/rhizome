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
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class PostgresIterable<T> implements Iterable<T> {
    private static final Map<Class<?>, Logger> subclassLoggers = new HashMap<>();

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
    public Iterator<T> iterator() {
        try {
            return new PostgresIterator<>( rsh.get(), mapper );
        } catch ( SQLException e ) {
            logger.error( "Error creating postgres stream iterator." );
            throw new IllegalStateException( "Unable to instantiate postgres iterator.", e );
        }
    }

    public Stream<T> stream() {
        return StreamUtil.stream( this );
    }

    public static class PostgresIterator<T> implements Iterator<T> {
        private static final Logger logger = LoggerFactory.getLogger( PostgresIterator.class );
        private final        Lock   lock   = new ReentrantLock();
        private final Function<ResultSet, T> mapper;
        private final StatementHolder        rsh;
        private final ResultSet              rs;
        private       boolean                notExhausted;

        public PostgresIterator( StatementHolder rsh, Function<ResultSet, T> mapper )
                throws SQLException {
            this.rsh = rsh;
            this.mapper = mapper;
            this.rs = rsh.getResultSet();
            notExhausted = this.rs.next();
        }

        @Override
        public boolean hasNext() {
            return notExhausted;
        }

        @Override
        public T next() {

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
                    try {
                        rsh.close();
                    } catch ( IOException e ) {
                        logger.error( "Error while closing result set." );
                    }
                }

                lock.unlock();
            }

            return nextElem;
        }
    }
}
