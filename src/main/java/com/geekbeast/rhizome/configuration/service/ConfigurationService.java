package com.geekbeast.rhizome.configuration.service;

import java.io.IOException;

import com.geekbeast.rhizome.configuration.Configuration;

/**
 * Configuration service API for getting, setting, and registering for configuration updates.
 * @author Matthew Tamayo-Rios
 */
public interface ConfigurationService {
    /**
     * Retrieves an existing configuration. The configuration class must have a static {@code getKey()} method 
     * that will be used as key to lookup the YAML configuration.   
     * @param clazz
     * @return The configuration if it can be found, null otherwise.
     * @throws IOException
     */
    public abstract <T extends Configuration> Configuration getConfiguration( Class<T> clazz ) throws IOException;
    /**
     * Creates or updates a configuration and fires a configuration update event to all subscribers.
     * @param configuration The configuration to be updated or created
     * @throws IOException
     */
    public abstract <T extends Configuration> void setConfiguration( T configuration ) throws IOException;
    /**
     * Registers a subscriber that will receive updated configuration events at methods annotated with guava's {@code @Subscribe} annotation.
     * @param subscriber
     */
//    public abstract <T extends Configuration> s<Configuration> getAllConfigurations();
    
    public abstract void subscribe( Object subscriber );
    
}