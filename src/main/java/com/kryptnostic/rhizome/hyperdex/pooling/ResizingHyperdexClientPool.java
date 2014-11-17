package com.kryptnostic.rhizome.hyperdex.pooling;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.hyperdex.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Queues;

public class ResizingHyperdexClientPool implements HyperdexClientPool {
    private static final Logger                 logger  = LoggerFactory.getLogger( ResizingHyperdexClientPool.class );
    private final ConcurrentLinkedQueue<Client> clients = Queues.newConcurrentLinkedQueue();
    private final HyperdexConfiguration         hyperdexConfiguration;

    public ResizingHyperdexClientPool( HyperdexConfiguration hyperdexConfiguration ) {
        this.hyperdexConfiguration = hyperdexConfiguration;
    }

    @Override
    public Client acquire() {
        Client c = clients.poll();
        if ( c == null ) {
            int port = hyperdexConfiguration.getPort();
            for ( String coordinator : hyperdexConfiguration.getCoordinators() ) {
                try {
                    c = new Client( coordinator, port );
                    return c;
                } catch ( Exception e ) {
                    logger.error(
                            "Unable to connect to hyperdex at {} on port {}... trying next coordinator.)",
                            coordinator,
                            port,
                            e );
                }
            }
        }
        return c;
    }

    @Override
    public Optional<Client> tryToAcquire() {
        return Optional.fromNullable( clients.poll() );
    }

    @Override
    public void release( Client c ) {
        clients.offer( c );
    }

}
