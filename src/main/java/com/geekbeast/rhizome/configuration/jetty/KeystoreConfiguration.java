package com.geekbeast.rhizome.configuration.jetty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class KeystoreConfiguration {
    private static final String PATH_PROPERTY     = "path";
    private static final String PASSWORD_PROPERTY = "password";

    private final String        storePath;
    private final String        storePassword;

    @JsonCreator
    public KeystoreConfiguration(
            @JsonProperty( PATH_PROPERTY ) Optional<String> storePath,
            @JsonProperty( PASSWORD_PROPERTY ) String storePassword ) {
        Preconditions.checkNotNull( storePassword, "Keystore password cannot be null." );

        this.storePath = storePath.or( "" );
        this.storePassword = storePassword;
    }

    @JsonProperty( PATH_PROPERTY )
    public String getStorePath() {
        return storePath;
    }

    @JsonProperty( PASSWORD_PROPERTY )
    public String getStorePassword() {
        return storePassword;
    }
}
