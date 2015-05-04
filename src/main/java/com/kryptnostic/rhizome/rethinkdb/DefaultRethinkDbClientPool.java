package com.kryptnostic.rhizome.rethinkdb;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Queues;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.RethinkDBConnection;

public class DefaultRethinkDbClientPool {
    private static final Logger                        logger  = LoggerFactory
                                                                       .getLogger( DefaultRethinkDbClientPool.class );

    private ConcurrentLinkedQueue<RethinkDBConnection> clients = Queues.newConcurrentLinkedQueue();
    private final RethinkDbConfiguration               config;

    public DefaultRethinkDbClientPool( RethinkDbConfiguration config ) {
        this.config = config;
    }

    public RethinkDBConnection acquire() {
        RethinkDBConnection c = clients.poll();

        if ( c == null ) {
            String hostname = config.getHostname();
            int port = config.getPort();

            RethinkDB r = RethinkDB.r;
            c = r.connect( hostname, port );
        }

        logger.debug( "Created rethink client, size is {}", clients.size() );
        return c;
    }

    public Optional<RethinkDBConnection> tryToAcquire() {
        return Optional.fromNullable( clients.poll() );
    }

    public void release( RethinkDBConnection c ) {
        logger.debug( "Released rethink client, size is {}", clients.size() );
        clients.offer( c );
    }

    public int available() {
        return clients.size();
    }

}
