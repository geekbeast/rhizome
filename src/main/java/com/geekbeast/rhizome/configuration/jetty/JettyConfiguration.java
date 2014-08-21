package com.geekbeast.rhizome.configuration.jetty;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey;
import com.google.common.base.Optional;

/**
 * @author Matthew Tamayo-Rios
 */
public class JettyConfiguration implements Configuration {
    private static final long serialVersionUID = 129440984814569272L;

    protected static ConfigurationKey key = new SimpleConfigurationKey( "jetty.yaml" );
    
    protected static final String KEYMANAGER_PASSWORD_PROPERTY = "keymanager-password";
    protected static final String MAX_THREADS_PROPERTY = "max-threads";
    protected static final String CONTEXT_CONFIGURATION_PROPERTY = "context";
    protected static final String KEYSTORE_CONFIGURATION_PROPERTY = "keystore";
    protected static final String TRUSTSTORE_CONFIGURATION_PROPERTY = "truststore";
    protected static final String WEB_ENDPOINT_CONFIGURATION_PROPERTY = "web-endpoint";
    protected static final String SERVICE_ENDPOINT_CONFIGURATION_PROPERTY = "service-endpoint";
    protected static final String GZIP_CONFIGURATION_PROPERTY = "gzip";
    protected static final int MAX_THREADS_DEFAULT = 500;
    
    
    protected final Optional<String> keymanagerPassword;
    protected final int maxThreads;
    protected final Optional<ConnectorConfiguration> webConnectorConfiguration;
    protected final Optional<ConnectorConfiguration> serviceConnectorConfiguration;
    protected final Optional<ContextConfiguration> contextConfiguration;
    protected final Optional<KeystoreConfiguration> keystoreConfiguration;
    protected final Optional<KeystoreConfiguration> truststoreConfiguration;
    protected final Optional<GzipConfiguration> gzipConfiguration;

    @JsonCreator
    public JettyConfiguration(
            @JsonProperty( WEB_ENDPOINT_CONFIGURATION_PROPERTY ) Optional<ConnectorConfiguration> webConnectorConfiguration,
            @JsonProperty( SERVICE_ENDPOINT_CONFIGURATION_PROPERTY ) Optional<ConnectorConfiguration> serviceConnectorConfiguration,
            @JsonProperty( MAX_THREADS_PROPERTY ) Optional<Integer> maxThreads,
            @JsonProperty( KEYMANAGER_PASSWORD_PROPERTY ) Optional<String> keymanagerPassword ,
            @JsonProperty( CONTEXT_CONFIGURATION_PROPERTY ) Optional<ContextConfiguration> contextConfiguration ,
            @JsonProperty( KEYSTORE_CONFIGURATION_PROPERTY ) Optional<KeystoreConfiguration> keystoreConfiguration ,
            @JsonProperty( TRUSTSTORE_CONFIGURATION_PROPERTY ) Optional<KeystoreConfiguration> truststoreConfiguration ,
            @JsonProperty( GZIP_CONFIGURATION_PROPERTY ) Optional<GzipConfiguration> gzipConfiguration
            ) {
        
        this.webConnectorConfiguration =  webConnectorConfiguration;
        this.serviceConnectorConfiguration = serviceConnectorConfiguration;
        
        this.keymanagerPassword = keymanagerPassword;
        this.maxThreads = maxThreads.or( MAX_THREADS_DEFAULT );;
        
        this.contextConfiguration = contextConfiguration;
        this.keystoreConfiguration = keystoreConfiguration;
        this.truststoreConfiguration = truststoreConfiguration;
        this.gzipConfiguration = gzipConfiguration;
    }
    
    public static ConfigurationKey key() {
        return key;
    }
    
    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key;
    }
   

    @JsonProperty( KEYMANAGER_PASSWORD_PROPERTY )
    public Optional<String> getKeyManagerPassword() {
        return keymanagerPassword;
    }
    
    @JsonProperty( CONTEXT_CONFIGURATION_PROPERTY  )
    public Optional<ContextConfiguration> getContextConfiguration() {
        return contextConfiguration;
    }

    @JsonProperty( KEYSTORE_CONFIGURATION_PROPERTY )
    public Optional<KeystoreConfiguration> getKeystoreConfiguration() {
        return keystoreConfiguration;
    }

    @JsonProperty( TRUSTSTORE_CONFIGURATION_PROPERTY )
    public Optional<KeystoreConfiguration> getTruststoreConfiguration() {
        return truststoreConfiguration;
    }
    
    @JsonProperty( WEB_ENDPOINT_CONFIGURATION_PROPERTY ) 
    public Optional<ConnectorConfiguration> getWebConnectorConfiguration() {
        return webConnectorConfiguration;
    }
    
    @JsonProperty( SERVICE_ENDPOINT_CONFIGURATION_PROPERTY ) 
    public Optional<ConnectorConfiguration> getServiceConnectorConfiguration() {
        return serviceConnectorConfiguration;
    }
    
    @JsonProperty( GZIP_CONFIGURATION_PROPERTY )
    public Optional<GzipConfiguration> getGzipConfiguration() {
        return gzipConfiguration;
    }
    
    @JsonProperty( MAX_THREADS_PROPERTY )
    public int getMaxThreads() {
        return maxThreads;
    }
    
}