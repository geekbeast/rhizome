package com.kryptnostic.rhizome.hyperdex.pooling;

import org.hyperdex.client.Client;

import com.google.common.base.Optional;

public interface HyperdexClientPool {
    Client acquire();
    Optional<Client> tryToAcquire();
    void release( Client c );
}
