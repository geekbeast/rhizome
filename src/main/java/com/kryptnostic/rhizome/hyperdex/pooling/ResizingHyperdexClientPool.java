package com.kryptnostic.rhizome.hyperdex.pooling;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.hyperdex.client.Client;
import org.hyperdex.client.HyperDexClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;

public class ResizingHyperdexClientPool implements HyperdexClientPool {
    private static final Logger           logger                  = LoggerFactory
                                                                          .getLogger( ResizingHyperdexClientPool.class );
    private static final String           HEALTH_CHECK_KEY        = "hyperdex-client-pool";
    private static final String           HEALTH_CHECK_DATA_FIELD = "data";
    private static final String           HEALTH_CHECK_VALUE      = "hyperdex-client-pool-is-good";

    private ConcurrentLinkedQueue<Client> clients                 = Queues.newConcurrentLinkedQueue();
    private final HyperdexConfiguration   hyperdexConfiguration;

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
        ConcurrentLinkedQueue<Client> oldClients = clients;
        clients = Queues.newConcurrentLinkedQueue();

        logger.info( "Starting health check of client pool ( size = {} ).", oldClients.size() );
        Stopwatch w = Stopwatch.createStarted();

        for ( Client c : oldClients ) {
            if ( isClientHealthy( c ) ) {
                release( c );
            }
        }
        logger.info( "Health check of client pool took {} ms ", w.elapsed( TimeUnit.MILLISECONDS ) );
    }

    public int available() {
        return clients.size();
    }

    private boolean isClientHealthy( Client c ) {
        try {
            Map<String, Object> result = c.get( hyperdexConfiguration.getHealthCheckKeyspace().get(), HEALTH_CHECK_KEY );
            if ( result != null && result.containsKey( HEALTH_CHECK_DATA_FIELD ) ) {
                return result.get( HEALTH_CHECK_DATA_FIELD ).toString().equals( HEALTH_CHECK_VALUE );
            } else {
                // Attempt to save value
                c.put(
                        hyperdexConfiguration.getHealthCheckKeyspace().get(),
                        HEALTH_CHECK_KEY,
                        ImmutableMap.of( HEALTH_CHECK_DATA_FIELD, HEALTH_CHECK_VALUE ) );
                release( c );
                return true;
            }
        } catch ( HyperDexClientException e ) {
            logger.info( "Health check failed for client." );
            return false;
        }
    }

}
