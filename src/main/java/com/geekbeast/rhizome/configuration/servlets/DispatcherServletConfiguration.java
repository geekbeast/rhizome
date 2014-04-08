package com.geekbeast.rhizome.configuration.servlets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class DispatcherServletConfiguration {
    private static final String SERVLET_NAME_PROPERTY = "servlet-name";
    private static final String SERVLET_MAPPINGS_PROPERTY = "mappings";
    private static final String LOAD_ON_STARTUP_PROPERTY = "load-on-startp";
    
    private final String servletName;
    private final String[] mappings;
    private final Optional<Integer> loadOnStartup;
    
    public DispatcherServletConfiguration(
           @JsonProperty( SERVLET_NAME_PROPERTY ) String servletName,
           @JsonProperty( SERVLET_MAPPINGS_PROPERTY ) String[] mappings,
           @JsonProperty( LOAD_ON_STARTUP_PROPERTY ) Optional<Integer> loadOnStartup
            ) {
        this.servletName = servletName;
        this.mappings = mappings;
        this.loadOnStartup = loadOnStartup;
    }
    
    @JsonProperty( SERVLET_NAME_PROPERTY )
    public String getServletName() {
        return servletName;
    }
    
    @JsonProperty( SERVLET_MAPPINGS_PROPERTY )
    public String[] getMappings() {
        return mappings;
    }

    @JsonProperty( LOAD_ON_STARTUP_PROPERTY )
    public Optional<Integer> getLoadOnStartup() {
        return loadOnStartup;
    }
}
