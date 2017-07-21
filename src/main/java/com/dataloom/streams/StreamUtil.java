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

package com.dataloom.streams;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class StreamUtil {
    public static <T> Stream<T> stream( Iterable<T> rs ) {
        return StreamSupport.stream( rs.spliterator(), false );
    }

    public static <T> Stream<T> parallelStream( Iterable<T> rs ) {
        return StreamSupport.stream( rs.spliterator(), true );
    }

    /**
     * Useful adapter for {@code Iterables#transform(Iterable, com.google.common.base.Function)} that allows lazy
     * evaluation of result set future. See the same function in AuthorizationUtils as well.
     * 
     * @param rsf The result set future to make a lazy evaluated iterator
     * @return The lazy evaluatable iterable
     */
    public static Iterable<Row> makeLazy( ResultSetFuture rsf ) {
        return getRowsAndFlatten( Stream.of( rsf ) )::iterator;
    }

    public static Stream<Row> getRowsAndFlatten( Stream<ResultSetFuture> stream ) {
        return stream.map( ResultSetFuture::getUninterruptibly )
                .flatMap( StreamUtil::stream );
    }
}
