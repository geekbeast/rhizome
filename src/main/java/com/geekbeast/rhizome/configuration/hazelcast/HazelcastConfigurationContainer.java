package com.geekbeast.rhizome.configuration.hazelcast;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;

@Immutable
public class HazelcastConfigurationContainer {

    private final Optional<Config> serverConfig;
    private final Optional<ClientConfig> clientConfig;

    public HazelcastConfigurationContainer( @CheckForNull Config server, @CheckForNull ClientConfig client ) {
        serverConfig = Optional.fromNullable( server );
        clientConfig = Optional.fromNullable( client );
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
