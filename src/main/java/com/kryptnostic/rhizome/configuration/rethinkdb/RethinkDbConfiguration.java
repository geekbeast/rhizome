package com.kryptnostic.rhizome.configuration.rethinkdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RethinkDbConfiguration {
    protected static final String FIELD_HOSTNAME = "hostname";
    protected static final String FIELD_PORT     = "port";

    private String                hostname;
    private int                   port;

    @JsonCreator
    public RethinkDbConfiguration( @JsonProperty( FIELD_HOSTNAME ) String hostname, @JsonProperty( FIELD_PORT ) int port ) {
        this.hostname = hostname;
        this.port = port;
    }

    @JsonProperty( FIELD_HOSTNAME )
    public String getHostname() {
        return hostname;
    }

    @JsonProperty( FIELD_PORT )
    public int getPort() {
        return port;
    }

}
