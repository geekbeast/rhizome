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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Deprecated( since = "Use PostgresIterable instead" )
public class KeyIterator<T> implements Iterator<T> {
    private static final Logger logger = LoggerFactory.getLogger( KeyIterator.class );
    private final ResultSet                 rs;
    private final CountdownConnectionCloser closer;
    private final SqlFunction<ResultSet, T> mapper;
    private       boolean                   next;

    public KeyIterator( ResultSet rs, CountdownConnectionCloser closer, SqlFunction<ResultSet, T> mapper ) {
        this.closer = closer;
        this.rs = rs;
        this.mapper = mapper;
        try {
            next = rs.next();
            countDownIfExhausted();
        } catch ( SQLException e ) {
            logger.error( "Unable to execute sql for load all keys for data map store" );
            throw new IllegalStateException( "Unable to execute sql statement", e );
        }
    }

    @Override public boolean hasNext() {
        return next;
    }

    @Override public T next() {
        T key;

        try {
            if ( next ) {
                key = mapper.apply( rs );
            } else {
                throw new NoSuchElementException( "No more elements available in iterator" );
            }
            next = rs.next();
        } catch ( SQLException e ) {
            logger.error( "Unable to retrieve next result from result set." );
            next = false;
            throw new IllegalStateException( "Unable to retrieve next result from result set.", e );
        } finally {
            countDownIfExhausted();
        }

        return key;
    }

    public void countDownIfExhausted() {
        if ( !next ) {
            closer.countDown();
        }
    }
}
