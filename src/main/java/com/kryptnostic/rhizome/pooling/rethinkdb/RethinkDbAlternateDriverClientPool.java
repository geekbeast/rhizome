package com.kryptnostic.rhizome.pooling.rethinkdb;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dkhenry.RethinkDB.RqlConnection;
import com.dkhenry.RethinkDB.errors.RqlDriverException;
import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Queues;

public class RethinkDbAlternateDriverClientPool {
    private static final Logger          logger  = LoggerFactory.getLogger( RethinkDbAlternateDriverClientPool.class );

    private final Queue<RqlConnection>   clients = Queues.newConcurrentLinkedQueue();
    private final RethinkDbConfiguration config;
    private final AtomicInteger          size    = new AtomicInteger();

    public RethinkDbAlternateDriverClientPool( RethinkDbConfiguration config ) {
        this.config = config;

    }

    private RqlConnection createClient() {
        String hostname = config.getHostname();
        int port = config.getPort();

        RqlConnection c = null;
        try {
            c = RqlConnection.connect( hostname, port );
            int sz = size.incrementAndGet();
            logger.debug( "Added rethink client, size is {} {}", sz, clients.size() );
        } catch ( RqlDriverException e ) {
            logger.error( "Failed to connect to RethinkDb {}", e );
        }
        return c;
    }

    public RqlConnection acquire() {
        RqlConnection c = null;
        int sz = size.get();

        logger.debug( "Attempting to acquire rethink client..., size is {} {}", sz, clients.size() );
        c = clients.poll();

        if ( c == null ) {
            c = createClient();
        }

        sz = size.get();
        logger.debug( "Acquired rethink client, size is {} {}", sz, clients.size() );
        return c;
    }

    public Optional<RqlConnection> tryToAcquire() {
        return Optional.fromNullable( clients.poll() );
    }

    public void release( RqlConnection c ) {
        clients.offer( c );
        int sz = size.get();
        logger.debug( "Released rethink client, size is {} {}", sz, clients.size() );
    }

    public int available() {
        return clients.size();
    }

}
