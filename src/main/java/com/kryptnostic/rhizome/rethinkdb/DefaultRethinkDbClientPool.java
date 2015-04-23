package com.kryptnostic.rhizome.rethinkdb;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dkhenry.RethinkDB.RqlConnection;
import com.dkhenry.RethinkDB.errors.RqlDriverException;
import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Queues;

public class DefaultRethinkDbClientPool {
    private static final Logger                  logger  = LoggerFactory.getLogger( DefaultRethinkDbClientPool.class );

    private ConcurrentLinkedQueue<RqlConnection> clients = Queues.newConcurrentLinkedQueue();
    private final RethinkDbConfiguration         config;

    public DefaultRethinkDbClientPool( RethinkDbConfiguration config ) {
        this.config = config;
    }

    public RqlConnection acquire() {
        RqlConnection c = clients.poll();

        if ( c == null ) {
            String hostname = config.getHostname();
            int port = config.getPort();

            try {
                c = RqlConnection.connect( hostname, port );
            } catch ( RqlDriverException e ) {
                logger.error( "Unable to connect to RethinkDB at {} on port {}...", hostname, port, e );
            }
        }

        logger.debug( "Created rethink client, size is {}", clients.size() );
        return c;
    }

    public Optional<RqlConnection> tryToAcquire() {
        return Optional.fromNullable( clients.poll() );
    }

    public void release( RqlConnection c ) {
        logger.debug( "Released rethink client, size is {}", clients.size() );
        clients.offer( c );
    }

    public int available() {
        return clients.size();
    }

}
