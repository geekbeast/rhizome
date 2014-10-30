package com.geekbeast.rhizome.configuration.graphite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * @author Matthew Tamayo-Rios
 */
public class GraphiteConfiguration {
    protected static final String GRAPHITE_HOST_PROPERTY            = "host";
    protected static final String GRAPHITE_PORT_PROPERTY            = "port";
    protected static final String GRAPHITE_GLOBAL_PREFIX_PROPERTY   = "global-prefix";
    protected static final String GRAPHITE_ENABLE_CONSOLE_REPORTING = "console-enabled";

    protected static final String GRAPHITE_HOST_DEFAULT             = "localhost";
    protected static final int    GRAPHITE_PORT_DEFAULT             = 2003;
    protected static final String GRAPHITE_GLOBAL_PREFIX_DEFAULT    = "rhizome";

    protected final String        graphiteHost;
    protected final int           graphitePort;
    protected final String        graphiteGlobalPrefix;
    protected final boolean       consoleEnabled;

    @JsonCreator
    public GraphiteConfiguration(
            @JsonProperty( GRAPHITE_HOST_PROPERTY ) Optional<String> graphiteHost,
            @JsonProperty( GRAPHITE_PORT_PROPERTY ) Optional<Integer> graphitePort,
            @JsonProperty( GRAPHITE_GLOBAL_PREFIX_PROPERTY ) Optional<String> graphiteGlobalPrefix,
            @JsonProperty( GRAPHITE_ENABLE_CONSOLE_REPORTING ) Optional<Boolean> consoleEnabled ) {
        this.graphiteHost = graphiteHost.or( GRAPHITE_HOST_DEFAULT );
        this.graphitePort = graphitePort.or( GRAPHITE_PORT_DEFAULT );
        this.graphiteGlobalPrefix = graphiteGlobalPrefix.or( GRAPHITE_GLOBAL_PREFIX_DEFAULT );
        this.consoleEnabled = consoleEnabled.or( false );
    }

    @JsonProperty( GRAPHITE_HOST_PROPERTY )
    public String getGraphiteHost() {
        return graphiteHost;
    }

    @JsonProperty( GRAPHITE_PORT_PROPERTY )
    public int getGraphitePort() {
        return graphitePort;
    }

    @JsonProperty( GRAPHITE_GLOBAL_PREFIX_PROPERTY )
    public String getGraphiteGlobalPrefix() {
        return graphiteGlobalPrefix;
    }
    
    @JsonProperty( GRAPHITE_ENABLE_CONSOLE_REPORTING )
    public boolean isEnableConsole() {
        return consoleEnabled;
    }
}
