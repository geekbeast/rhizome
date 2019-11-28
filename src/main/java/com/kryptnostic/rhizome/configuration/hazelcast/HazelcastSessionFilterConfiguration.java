package com.kryptnostic.rhizome.configuration.hazelcast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @author Matthew Tamayo-Rios
 */
public class HazelcastSessionFilterConfiguration {
    public final static String   CLIENT_CONFIG_LOCATION_PROPERTY = "client-config-location";
    public final static String   USE_CLIENT_PROPERTY             = "use-client";
    public final static String   MAP_NAME_PROPERTY               = "map-name";
    public final static String   STICKY_SESSION_PROPERTY         = "sticky-session";
    public final static String   COOKIE_NAME_PROPERTY            = "cookie-name";
    public final static String   COOKIE_SECURE_PROPERTY          = "cookie-secure";
    public final static String   COOKIE_HTTP_ONLY_PROPERTY       = "cookie-http-only";
    public final static String   DEBUG_PROPERTY                  = "debug";
    public final static String   INSTANCE_NAME_PROPERTY          = "instance-name";
    public final static String   SHUTDOWN_ON_DESTROY_PROPERTY    = "shutdown-on-destroy";

    private final static String  CLIENT_CONFIG_LOCATION_DEFAULT  = "hazelcast-session-client.xml";
    private final static boolean USE_CLIENT_DEFAULT              = true;
    private final static String  MAP_NAME_DEFAULT                = "sessions";
    private final static boolean STICKY_SESSION_DEFAULT          = false;
    private final static String  COOKIE_NAME_DEFAULT             = "hazelcast.sessionId";
    private final static boolean COOKIE_SECURE_DEFAULT           = false;
    private final static boolean COOKIE_HTTP_ONLY_DEFAULT        = false;
    private final static boolean DEBUG_DEFAULT                   = false;
    private final static String  INSTANCE_NAME_DEFAULT           = "hazelcastInstance";
    private final static boolean SHUTDOWN_ON_DESTROY_DEFAULT     = true;

    private final String         clientConfigLocation;
    private final boolean        useClient;
    private final String         mapName;
    private final boolean        stickySession;
    private final String         cookieName;
    private final boolean        cookieSecure;
    private final boolean        cookieHttpOnly;
    private final boolean        debug;
    private final String         instanceName;
    private final boolean        shutdownOnDestroy;

    public HazelcastSessionFilterConfiguration() {
        this.clientConfigLocation = CLIENT_CONFIG_LOCATION_DEFAULT;
        this.useClient = USE_CLIENT_DEFAULT;
        this.mapName = MAP_NAME_DEFAULT;
        this.stickySession = STICKY_SESSION_DEFAULT;
        this.cookieName = COOKIE_NAME_DEFAULT;
        this.cookieSecure = COOKIE_SECURE_DEFAULT;
        this.cookieHttpOnly = COOKIE_HTTP_ONLY_DEFAULT;
        this.debug = DEBUG_DEFAULT;
        this.instanceName = INSTANCE_NAME_DEFAULT;
        this.shutdownOnDestroy = SHUTDOWN_ON_DESTROY_DEFAULT;
    }

    @JsonCreator
    public HazelcastSessionFilterConfiguration(
            @JsonProperty( CLIENT_CONFIG_LOCATION_PROPERTY ) Optional<String> clientConfigLocation,
            @JsonProperty( USE_CLIENT_PROPERTY ) Optional<Boolean> useClient,
            @JsonProperty( MAP_NAME_PROPERTY ) Optional<String> mapName,
            @JsonProperty( STICKY_SESSION_PROPERTY ) Optional<Boolean> stickySession,
            @JsonProperty( COOKIE_NAME_PROPERTY ) Optional<String> cookieName,
            @JsonProperty( COOKIE_SECURE_PROPERTY ) Optional<Boolean> cookieSecure,
            @JsonProperty( COOKIE_HTTP_ONLY_PROPERTY ) Optional<Boolean> cookieHttpOnly,
            @JsonProperty( DEBUG_PROPERTY ) Optional<Boolean> debug,
            @JsonProperty( INSTANCE_NAME_PROPERTY ) Optional<String> instanceName,
            @JsonProperty( SHUTDOWN_ON_DESTROY_PROPERTY ) Optional<Boolean> shutdownOnDestroy ) {

        this.clientConfigLocation = clientConfigLocation.orElse( CLIENT_CONFIG_LOCATION_DEFAULT );
        this.useClient = useClient.orElse( USE_CLIENT_DEFAULT );
        this.mapName = mapName.orElse( MAP_NAME_DEFAULT );
        this.stickySession = stickySession.orElse( STICKY_SESSION_DEFAULT );
        this.cookieName = cookieName.orElse( COOKIE_NAME_DEFAULT );
        this.cookieSecure = cookieSecure.orElse( COOKIE_SECURE_DEFAULT );
        this.cookieHttpOnly = cookieHttpOnly.orElse( COOKIE_HTTP_ONLY_DEFAULT );
        this.debug = debug.orElse( DEBUG_DEFAULT );
        this.instanceName = instanceName.orElse( INSTANCE_NAME_DEFAULT );
        this.shutdownOnDestroy = shutdownOnDestroy.orElse( SHUTDOWN_ON_DESTROY_DEFAULT );

        Preconditions.checkArgument( !StringUtils.isWhitespace( this.clientConfigLocation ) );
        Preconditions.checkArgument( !StringUtils.isWhitespace( this.mapName ) );
        Preconditions.checkArgument( !StringUtils.isWhitespace( this.cookieName ) );
        Preconditions.checkArgument( !StringUtils.isWhitespace( this.instanceName ) );
    }

    @JsonProperty( CLIENT_CONFIG_LOCATION_PROPERTY )
    public String getClientConfigLocation() {
        return clientConfigLocation;
    }

    @JsonProperty( USE_CLIENT_PROPERTY )
    public boolean useClient() {
        return useClient;
    }

    @JsonProperty( MAP_NAME_PROPERTY )
    public String getMapName() {
        return mapName;
    }

    @JsonProperty( STICKY_SESSION_PROPERTY )
    public boolean isStickySession() {
        return stickySession;
    }

    @JsonProperty( COOKIE_NAME_PROPERTY )
    public String getCookieName() {
        return cookieName;
    }

    @JsonProperty( COOKIE_SECURE_PROPERTY )
    public boolean isCookieSecure() {
        return cookieSecure;
    }

    @JsonProperty( COOKIE_HTTP_ONLY_PROPERTY )
    public boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }

    @JsonProperty( DEBUG_PROPERTY )
    public boolean isDebug() {
        return debug;
    }

    @JsonProperty( INSTANCE_NAME_PROPERTY )
    public String getInstanceName() {
        return instanceName;
    }

    @JsonProperty( SHUTDOWN_ON_DESTROY_PROPERTY )
    public boolean isShutdownOnDestroy() {
        return shutdownOnDestroy;
    }

}
