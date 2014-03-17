package com.geekbeast.rhizome.configuration.jetty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey;
import com.google.common.base.Optional;

public class JettyConfiguration implements Configuration {
    private static final long serialVersionUID = 5525308652220152800L;
    protected static ConfigurationKey key = new SimpleConfigurationKey( "jetty.yaml" );
    

    protected static final String KEYMANAGER_PASSWORD_PROPERTY = "keymanager-password";
    protected static final String MAX_THREADS_PROPERTY = "max-threads";
    protected static final String CONTEXT_CONFIGURATION_PROPERTY = "context";
    protected static final String KEYSTORE_CONFIGURATION_PROPERTY = "keystore";
    protected static final String TRUSTSTORE_CONFIGURATION_PROPERTY = "truststore";
    protected static final String WEB_ENDPOINT_CONFIGURATION_PROPERTY = "web-endpoint";
    protected static final String SERVICE_ENDPOINT_CONFIGURATION_PROPERTY = "service-endpoint";
    protected static final String GZIP_CONFIGURATION_PROPERTY = "gzip";
    
    protected final String keymanagerPassword;
    protected final int maxThreads;
    protected final EndpointConfiguration webEndpointConfiguration;
    protected final EndpointConfiguration serviceEndpointConfiguration;
    protected final ContextConfiguration contextConfiguration;
    protected final KeystoreConfiguration keystoreConfiguration;
    protected final KeystoreConfiguration truststoreConfiguration;
    protected final Optional<GzipConfiguration> gzipConfiguration;

    @JsonCreator
    public JettyConfiguration(
            @JsonProperty( WEB_ENDPOINT_CONFIGURATION_PROPERTY ) EndpointConfiguration webEndpointConfiguration,
            @JsonProperty( SERVICE_ENDPOINT_CONFIGURATION_PROPERTY ) EndpointConfiguration serviceEndpointConfiguration,
            @JsonProperty( MAX_THREADS_PROPERTY ) int maxThreads,
            @JsonProperty( KEYMANAGER_PASSWORD_PROPERTY ) String keymanagerPassword ,
            @JsonProperty( CONTEXT_CONFIGURATION_PROPERTY ) ContextConfiguration contextConfiguration ,
            @JsonProperty( KEYSTORE_CONFIGURATION_PROPERTY ) KeystoreConfiguration keystoreConfiguration ,
            @JsonProperty( TRUSTSTORE_CONFIGURATION_PROPERTY ) KeystoreConfiguration truststoreConfiguration ,
            @JsonProperty( GZIP_CONFIGURATION_PROPERTY ) Optional<GzipConfiguration> gzipConfiguration
            ) {
        
        this.webEndpointConfiguration =  webEndpointConfiguration;
        this.serviceEndpointConfiguration = serviceEndpointConfiguration;
        
        this.keymanagerPassword = keymanagerPassword;
        this.maxThreads = maxThreads;
        
        this.contextConfiguration = contextConfiguration;
        this.keystoreConfiguration = keystoreConfiguration;
        this.truststoreConfiguration = truststoreConfiguration;
        this.gzipConfiguration = gzipConfiguration;
    }
    
    @JsonProperty( KEYMANAGER_PASSWORD_PROPERTY )
    public String getKeyManagerPassword() {
        return keymanagerPassword;
    }
    
    @JsonProperty( CONTEXT_CONFIGURATION_PROPERTY  )
    public ContextConfiguration getContextConfiguration() {
        return contextConfiguration;
    }

    @JsonProperty( KEYSTORE_CONFIGURATION_PROPERTY )
    public KeystoreConfiguration getKeystoreConfiguration() {
        return keystoreConfiguration;
    }

    @JsonProperty( TRUSTSTORE_CONFIGURATION_PROPERTY )
    public KeystoreConfiguration getTruststoreConfiguration() {
        return truststoreConfiguration;
    }
    
    @JsonProperty( WEB_ENDPOINT_CONFIGURATION_PROPERTY ) 
    public EndpointConfiguration getWebEndpointConfiguration() {
        return webEndpointConfiguration;
    }
    
    @JsonProperty( SERVICE_ENDPOINT_CONFIGURATION_PROPERTY ) 
    public EndpointConfiguration getServiceEndpointConfiguration() {
        return serviceEndpointConfiguration;
    }
    
    @JsonProperty( GZIP_CONFIGURATION_PROPERTY )
    public Optional<GzipConfiguration> getGzipConfiguration() {
        return gzipConfiguration;
    }
    
    @JsonProperty( MAX_THREADS_PROPERTY )
    public int getMaxThreads() {
        return maxThreads;
    }
    
    public static ConfigurationKey key() {
        return key;
    }
    
    @Override
    public ConfigurationKey getKey() {
        return key;
    }    
}
