package com.kryptnostic.rhizome.configuration.hyperdex;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class HyperdexConfiguration {
    private static final String       HYPERDEX_PORT_PROPERTY                  = "port";
    private static final String       HYPERDEX_COORDINATORS_PROPERTY          = "coordinators";
    private static final String       HYPERDEX_NATIVE_BINARY_FOLDER_PROPERTY  = "native-bin-path";
    private static final String       RHIZOME_CONFIGURATION_KEYSPACE_PROPERTY = "configuration-keyspace";
    private static final String       HYPERDEX_HEALTCHECK_KEYSPACE_PROPERTY   = "healthcheck-keyspace";

    private static final List<String> HYPERDEX_SEED_DEFAULT                   = ImmutableList.of( "127.0.0.1" );
    private static final int          PORT_DEFAULT                            = 1982;
    private static final String       NATIVE_BINARY_FOLDER_DEFAULT            = HyperdexPreconfigurer.DEFAULT_HYPERDEX_LIB_DIRECTORY;

    private final List<String>        coordinators;
    private final int                 port;
    private final String              nativeBinPath;
    private final Optional<String>    configurationKeyspace;
    private final Optional<String>    healthCheckKeyspace;

    @JsonCreator
    public HyperdexConfiguration(
            @JsonProperty( HYPERDEX_COORDINATORS_PROPERTY ) Optional<List<String>> coordinators,
            @JsonProperty( HYPERDEX_PORT_PROPERTY ) Optional<Integer> port,
            @JsonProperty( HYPERDEX_NATIVE_BINARY_FOLDER_PROPERTY ) Optional<String> nativeBinPath,
            @JsonProperty( RHIZOME_CONFIGURATION_KEYSPACE_PROPERTY ) Optional<String> configurationKeyspace,
            @JsonProperty( HYPERDEX_HEALTCHECK_KEYSPACE_PROPERTY ) Optional<String> healthCheckKeyspace ) {

        this.coordinators = coordinators.or( HYPERDEX_SEED_DEFAULT );
        this.port = port.or( PORT_DEFAULT );
        this.nativeBinPath = nativeBinPath.or( NATIVE_BINARY_FOLDER_DEFAULT );
        this.configurationKeyspace = configurationKeyspace;
        this.healthCheckKeyspace = healthCheckKeyspace;
    }

    @JsonProperty( HYPERDEX_COORDINATORS_PROPERTY )
    public List<String> getCoordinators() {
        return coordinators;
    }

    @JsonProperty( HYPERDEX_PORT_PROPERTY )
    public int getPort() {
        return port;
    }

    @JsonProperty( HYPERDEX_NATIVE_BINARY_FOLDER_PROPERTY )
    public String getNativeBinPath() {
        return nativeBinPath;
    }

    @JsonProperty( RHIZOME_CONFIGURATION_KEYSPACE_PROPERTY )
    public Optional<String> getConfigurationKeyspace() {
        return configurationKeyspace;
    }

    @JsonProperty( HYPERDEX_HEALTCHECK_KEYSPACE_PROPERTY )
    public Optional<String> getHealthCheckKeyspace() {
        return healthCheckKeyspace;
    }
}
