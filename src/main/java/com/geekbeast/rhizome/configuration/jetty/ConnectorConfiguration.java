package com.geekbeast.rhizome.configuration.jetty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.Random;

/**
 * @author Matthew Tamayo-Rios
 */
@JsonIgnoreProperties(
        ignoreUnknown = false )
public class ConnectorConfiguration {
    protected static final String HTTP_PORT_PROPERTY         = "http-port";
    protected static final String HTTPS_PORT_PROPERTY        = "https-port";
    protected static final String USE_SSL_PROPERTY           = "use-ssl";
    protected static final String REQUIRE_SSL_PROPERTY       = "require-ssl";
    protected static final String NEED_CLIENT_AUTH_PROPERTY  = "require-client-auth";
    protected static final String WANT_CLIENT_AUTH_PROPERTY  = "want-client-auth";
    protected static final String CERTIFICATE_ALIAS_PROPERTY = "certificate-alias";
    protected static final String HTTP2_ENABLED_PROPERTY     = "http2-enabled";

    protected static final int     HTTP_PORT_DEFAULT        = 8081;
    protected static final int     SSL_PORT_DEFAULT         = 8443;
    protected static final boolean NEED_CLIENT_AUTH_DEFAULT = false;
    protected static final boolean WANT_CLIENT_AUTH_DEFAULT = false;
    protected static final boolean USE_SSL_DEFAULT          = false;
    protected static final boolean REQUIRE_SSL_DEFAULT      = false;
    protected static final boolean HTTP2_ENABLED_DEFAULT    = true;

    protected final boolean          needClientAuth;
    protected final boolean          wantClientAuth;
    protected final boolean          useSSL;
    protected final boolean          requireSSL;
    protected final Optional<String> certificateAlias;
    protected final boolean          http2Enabled;
    protected       int              httpPort;
    protected       int              httpsPort;

    /*
     * Pass in 0 for port numbers to automatically generate random ports for unit testing / integration testing.
     */

    @JsonCreator
    public ConnectorConfiguration(
            @JsonProperty(
                    value = HTTP_PORT_PROPERTY,
                    required = true ) Optional<Integer> httpPort,
            @JsonProperty(
                    value = HTTPS_PORT_PROPERTY,
                    required = true ) Optional<Integer> httpsPort,
            @JsonProperty( HTTP2_ENABLED_PROPERTY ) Optional<Boolean> http2Enabled,
            @JsonProperty( USE_SSL_PROPERTY ) Optional<Boolean> useSSL,
            @JsonProperty( REQUIRE_SSL_PROPERTY ) Optional<Boolean> requireSSL,
            @JsonProperty( NEED_CLIENT_AUTH_PROPERTY ) Optional<Boolean> needClientAuth,
            @JsonProperty( WANT_CLIENT_AUTH_PROPERTY ) Optional<Boolean> wantClientAuth,
            @JsonProperty( CERTIFICATE_ALIAS_PROPERTY ) Optional<String> certificateAlias ) {
        final Random r = new Random( System.currentTimeMillis() );

        if ( httpPort.isPresent() && httpPort.get() == 0 ) {
            this.httpPort = HTTP_PORT_DEFAULT + r.nextInt( 50 );
        } else {
            this.httpPort = httpPort.orElse( HTTP_PORT_DEFAULT );
        }

        if ( httpsPort.isPresent() && httpsPort.get() == 0 ) {
            this.httpsPort = SSL_PORT_DEFAULT + r.nextInt( 50 );
        } else {
            this.httpsPort = httpsPort.orElse( SSL_PORT_DEFAULT );
        }

        this.http2Enabled = http2Enabled.orElse( HTTP2_ENABLED_DEFAULT );
        this.useSSL = useSSL.orElse( USE_SSL_DEFAULT );
        this.requireSSL = requireSSL.orElse( REQUIRE_SSL_DEFAULT );
        this.needClientAuth = needClientAuth.orElse( NEED_CLIENT_AUTH_DEFAULT );
        this.wantClientAuth = wantClientAuth.orElse( WANT_CLIENT_AUTH_DEFAULT );
        this.certificateAlias = certificateAlias;
    }

    @JsonProperty( HTTP2_ENABLED_PROPERTY )
    public boolean isHttp2Enabled() {
        return http2Enabled;
    }

    @JsonProperty( HTTP_PORT_PROPERTY )
    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort( int httpPort ) {
        this.httpPort = httpPort;
    }

    @JsonProperty( HTTPS_PORT_PROPERTY )
    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort( int httpsPort ) {
        this.httpsPort = httpsPort;
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

    @JsonProperty( CERTIFICATE_ALIAS_PROPERTY )
    public Optional<String> getCertificateAlias() {
        return certificateAlias;
    }
}
