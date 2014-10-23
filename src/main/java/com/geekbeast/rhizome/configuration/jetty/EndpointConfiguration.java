package com.geekbeast.rhizome.configuration.jetty;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

@JsonIgnoreProperties(
    ignoreUnknown = false )
public class EndpointConfiguration {
    protected static final String  HTTP_PORT_PROPERTY        = "http-port";
    protected static final String  HTTPS_PORT_PROPERTY       = "https-port";
    protected static final String  USE_SSL_PROPERTY          = "use-ssl";
    protected static final String  REQUIRE_SSL_PROPERTY      = "require-ssl";
    protected static final String  NEED_CLIENT_AUTH_PROPERTY = "require-client-auth";
    protected static final String  WANT_CLIENT_AUTH_PROPERTY = "want-client-auth";

    protected static final int     HTTP_PORT_DEFAULT         = 8081;
    protected static final int     SSL_PORT_DEFAULT          = 8443;
    protected static final boolean NEED_CLIENT_AUTH_DEFAULT  = false;
    protected static final boolean WANT_CLIENT_AUTH_DEFAULT  = false;
    protected static final boolean USE_SSL_DEFAULT           = false;
    protected static final boolean REQUIRE_SSL_DEFAULT       = false;

    protected final int            httpPort;
    protected final int            httpsPort;
    protected final boolean        needClientAuth;
    protected final boolean        wantClientAuth;
    protected final boolean        useSSL;
    protected final boolean        requireSSL;

    /*
     * Pass in 0 for port numbers to automatically generate random ports for unit testing / integration testing.
     */

    @JsonCreator
    public EndpointConfiguration(
            @JsonProperty(
                value = HTTP_PORT_PROPERTY,
                required = true ) Optional<Integer> httpPort,
            @JsonProperty(
                value = HTTPS_PORT_PROPERTY,
                required = true ) Optional<Integer> httpsPort,
            @JsonProperty( USE_SSL_PROPERTY ) Optional<Boolean> useSSL,
            @JsonProperty( REQUIRE_SSL_PROPERTY ) Optional<Boolean> requireSSL,
            @JsonProperty( NEED_CLIENT_AUTH_PROPERTY ) Optional<Boolean> needClientAuth,
            @JsonProperty( WANT_CLIENT_AUTH_PROPERTY ) Optional<Boolean> wantClientAuth ) {
        final Random r = new Random( System.currentTimeMillis() );

        if ( httpPort.isPresent() && httpPort.get() == 0 ) {
            this.httpPort = HTTP_PORT_DEFAULT + r.nextInt( 50 );
        } else {
            this.httpPort = httpPort.or( HTTP_PORT_DEFAULT );
        }

        if ( httpsPort.isPresent() && httpsPort.get() == 0 ) {
            this.httpsPort = SSL_PORT_DEFAULT + r.nextInt( 50 );
        } else {
            this.httpsPort = httpsPort.or( SSL_PORT_DEFAULT );
        }

        this.useSSL = useSSL.or( USE_SSL_DEFAULT );
        this.requireSSL = requireSSL.or( REQUIRE_SSL_DEFAULT );
        this.needClientAuth = needClientAuth.or( NEED_CLIENT_AUTH_DEFAULT );
        this.wantClientAuth = wantClientAuth.or( WANT_CLIENT_AUTH_DEFAULT );
    }

    @JsonProperty( HTTP_PORT_PROPERTY )
    public int getHttpPort() {
        return httpPort;
    }

    @JsonProperty( HTTPS_PORT_PROPERTY )
    public int getHttpsPort() {
        return httpsPort;
    }

    @JsonProperty( NEED_CLIENT_AUTH_PROPERTY )
    public boolean needClientAuth() {
        return needClientAuth;
    }

    @JsonProperty( WANT_CLIENT_AUTH_PROPERTY )
    public boolean wantClientAuth() {
        return wantClientAuth;
    }

    @JsonProperty( USE_SSL_PROPERTY )
    public boolean useSSL() {
        return useSSL;
    }

    @JsonProperty( REQUIRE_SSL_PROPERTY )
    public boolean requireSSL() {
        return requireSSL;
    }
}
