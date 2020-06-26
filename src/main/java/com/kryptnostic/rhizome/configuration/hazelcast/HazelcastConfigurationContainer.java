package com.kryptnostic.rhizome.configuration.hazelcast;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
public class HazelcastConfigurationContainer {

    private final Optional<Config>       serverConfig;
    private final Optional<ClientConfig> clientConfig;

    public HazelcastConfigurationContainer( @CheckForNull Config server, @CheckForNull ClientConfig client ) {
        serverConfig = Optional.ofNullable( server );
        clientConfig = Optional.ofNullable( client );
    }

    @CheckForNull
    public Optional<Config> getServerConfig() {
        return serverConfig;
    }

    @CheckForNull
    public Optional<ClientConfig> getClientConfig() {
        return clientConfig;
    }
}
