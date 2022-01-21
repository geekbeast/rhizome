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

package com.geekbeast.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Deprecated( since = "Use Base/PostgresIterable instead" )
public class CountdownConnectionCloser {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Logger          logger   = LoggerFactory.getLogger( CountdownConnectionCloser.class );
    private final CountDownLatch latch;

    public CountdownConnectionCloser( ResultSet rs, Connection connection, int count ) {
        this.latch = new CountDownLatch( count );
        executor.execute( () -> {
            try {
                latch.await();
                rs.close();
                connection.close();
            } catch ( InterruptedException e ) {
                logger.error( "Interrupted while waiting to close connection.", e );
            } catch ( SQLException e ) {
                logger.error( "Error while closing connection.", e );
            }
        } );
    }

    public CountdownConnectionCloser( Connection connection, int count ) {
        this.latch = new CountDownLatch( count );
        executor.execute( () -> {
            try {
                latch.await();
                connection.close();
            } catch ( InterruptedException e ) {
                logger.error( "Interrupted while waiting to close connection.", e );
            } catch ( SQLException e ) {
                logger.error( "Error while closing connection.", e );
            }
        } );
    }

    public void countDown() {
        latch.countDown();
        logger.info( "Latch value: {}", latch.getCount() );
    }
}
