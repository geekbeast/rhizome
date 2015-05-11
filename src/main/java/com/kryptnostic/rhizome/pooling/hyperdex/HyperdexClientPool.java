package com.kryptnostic.rhizome.pooling.hyperdex;

import javax.annotation.Nullable;

import org.hyperdex.client.Client;

import com.google.common.base.Optional;

public interface HyperdexClientPool {

    /**
     * Attempts to acquire a client from the pool. May return null if none are available.
     * 
     * @return
     */
    @Nullable
    Client acquire();

    /**
     * Attempts to acquire a client from the pool. Equivalent to Optional.fromNullable( acquire() )
     * 
     * @return
     */
    Optional<Client> tryToAcquire();

    /**
     * Releases a client back to the pool for re-use
     * 
     * @param c The client to release back to the pool.
     */
    void release( Client c );

    /**
     * Get the number of available clients in the pool.
     * 
     * @return The number of clients currently available in the pool.
     */
    int available();
}
