package com.kryptnostic.rhizome.hyperdex.pooling;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import jersey.repackaged.com.google.common.collect.Sets;

import org.hyperdex.client.Client;
import org.hyperdex.client.HyperDexClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Queues;

public class ResizingHyperdexClientPool implements HyperdexClientPool {
    private static final Logger                 logger                  = LoggerFactory
                                                                                .getLogger( ResizingHyperdexClientPool.class );
    private static final String                 HEALTH_CHECK_KEY        = "hyperdex-client-pool";
    private static final String                 HEALTH_CHECK_DATA_FIELD = "data";
    private static final String                 HEALTH_CHECK_VALUE      = "hyperdex-client-pool-is-good";

    private final ConcurrentLinkedQueue<Client> clients                 = Queues.newConcurrentLinkedQueue();
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

    // Every five minutes ping and purge bad clients.
    @Scheduled(
        fixedRate = 5 * 60000 )
    public void ping() {
        Client c = acquire(); 
        Set<Client> stopSet = Sets.newHashSet();
        
        while ( !stopSet.contains( c ) ) {
            if ( isClientHealthy( c ) ) {
                release( c );
                stopSet.add( c );
            }
            c = acquire();
        }
    }
    
    public int size() {
        return clients.size();
    }
    
    private boolean isClientHealthy( Client c ) {
        try {
            return c.get( hyperdexConfiguration.getHealthCheckKeyspace().get() , HEALTH_CHECK_KEY ).get( HEALTH_CHECK_DATA_FIELD ).toString()
                    .equals( HEALTH_CHECK_VALUE );
        } catch ( HyperDexClientException e ) {
            return false;
        }
    }

}
