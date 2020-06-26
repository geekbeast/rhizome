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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class StreamUtil {
    private static final Logger logger = LoggerFactory.getLogger( StreamUtil.class );

    private StreamUtil() {
    }

    public static <T> Stream<T> stream( Iterable<T> rs ) {
        return StreamSupport.stream( rs.spliterator(), false );
    }

    public static <T> Stream<T> parallelStream( Iterable<T> rs ) {
        return StreamSupport.stream( rs.spliterator(), true );
    }

    public static <T> T getUninterruptibly( ListenableFuture<T> f ) {
        try {
            return Uninterruptibles.getUninterruptibly( f );
        } catch ( ExecutionException e ) {
            logger.error( "Unable to get future!", e );
            return null;
        }
    }
}

