package com.geekbeast.rhizome.configuration.servlets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey;

public class SpringDispatcherServlets implements Configuration {
    private static final long serialVersionUID = 4165678301627183197L;
    protected static final ConfigurationKey key = new SimpleConfigurationKey( "jetty.yaml" );
    
    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key();
    }

    public static final ConfigurationKey key() {
        return key;
    }    
}
