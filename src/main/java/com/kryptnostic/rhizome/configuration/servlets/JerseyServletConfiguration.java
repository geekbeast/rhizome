package com.kryptnostic.rhizome.configuration.servlets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class JerseyServletConfiguration {
    private static final String     SERVLET_NAME_PROPERTY      = "servlet-name";
    private static final String     APPLICATION_CLASS_PROPERTY = "application-class";
    private static final String     MAPPINGS_PROPERTY          = "mappings";
    private static final String     LOAD_ON_STARTUP_PROPERTY   = "load-on-startp";

    private final String            servletName;
    private final String            applicationClass;
    private final String[]          mappings;
    private final Optional<Integer> loadOnStartup;

    public JerseyServletConfiguration(
            @JsonProperty( SERVLET_NAME_PROPERTY ) String servletName,
            @JsonProperty( APPLICATION_CLASS_PROPERTY ) String applicationClass,
            @JsonProperty( MAPPINGS_PROPERTY ) String[] mappings,
            @JsonProperty( LOAD_ON_STARTUP_PROPERTY ) Optional<Integer> loadOnStartup ) {

        this.servletName = servletName;
        this.applicationClass = applicationClass;
        this.mappings = mappings;
        this.loadOnStartup = loadOnStartup;
    }

    @JsonProperty( SERVLET_NAME_PROPERTY )
    public String getServletName() {
        return servletName;
    }

    @JsonProperty( APPLICATION_CLASS_PROPERTY )
    public String getApplicationClass() {
        return applicationClass;
    }

    @JsonProperty( MAPPINGS_PROPERTY )
    public String[] getMappings() {
        return mappings;
    }

    @JsonProperty( LOAD_ON_STARTUP_PROPERTY )
    public Optional<Integer> getLoadOnStartup() {
        return loadOnStartup;
    }
}
