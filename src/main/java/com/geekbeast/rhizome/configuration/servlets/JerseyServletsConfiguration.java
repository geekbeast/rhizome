package com.geekbeast.rhizome.configuration.servlets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey;

public class JerseyServletsConfiguration implements Configuration {
    private static final long serialVersionUID = 5147810292165300316L;
    private static final String SERVLET_NAME_PROPERTY = "servlet-name";
    private static final String APPLICATION_CLASS_PROPERTY = "application-class";
    private static final String MAPPINGS_PROPETY = "mappings";
    
    protected static final ConfigurationKey key = SimpleConfigurationKey.fromUri( "jersey-servlets.yaml" );
    
    private final String servletName;
    private final String applicationClass;
    private final String[] mappings;



    public JerseyServletsConfiguration(
            @JsonProperty( SERVLET_NAME_PROPERTY ) String servletName,
            @JsonProperty( APPLICATION_CLASS_PROPERTY ) String applicationClass,
            @JsonProperty( MAPPINGS_PROPETY ) String[] mappings
            ) {
        
        this.servletName = servletName;
        this.applicationClass = applicationClass;
        this.mappings = mappings;
    }
    
    @JsonProperty( SERVLET_NAME_PROPERTY ) 
    public String getServletName() {
        return servletName;
    }

    @JsonProperty( APPLICATION_CLASS_PROPERTY )
    public String getApplicationClass() {
        return applicationClass;
    }

    @JsonProperty( MAPPINGS_PROPETY )
    public String[] getMappings() {
        return mappings;
    }
        
    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key();
    }

    public static final ConfigurationKey key() {
        return key;
    }    
}
