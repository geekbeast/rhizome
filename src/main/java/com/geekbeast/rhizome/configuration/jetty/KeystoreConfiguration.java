package com.geekbeast.rhizome.configuration.jetty;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class KeystoreConfiguration {
    private static final String PATH_PROPERTY = "path";
    private static final String PASSWORD_PROPERTY = "password";
    
    private final String storePath;
    private final String storePassword;
    
    @JsonCreator
    public KeystoreConfiguration( 
            @JsonProperty( PATH_PROPERTY ) String storePath ,
            @JsonProperty( PASSWORD_PROPERTY ) String storePassword 
            ) {
        
        Preconditions.checkArgument( StringUtils.isNotBlank( storePath ) , "Path to keystore cannot be blank.");
        Preconditions.checkNotNull( storePassword , "Keystore password cannot be null.");
        
        this.storePath = storePath;
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
