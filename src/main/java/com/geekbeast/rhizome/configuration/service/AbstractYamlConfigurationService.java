package com.geekbeast.rhizome.configuration.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import javax.annotation.Nullable;
import javax.el.MethodNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.registries.ObjectMapperRegistry;
import com.geekbeast.rhizome.utils.RhizomeUtils;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;

/**
 * Abstract class for loading YAML format configuration classes that handles low level resource reading, validation, and update publishing.
 * @author Matthew Tamayo-Rios
 */
//TODO: Add hibernate validation
public abstract class AbstractYamlConfigurationService implements ConfigurationService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractYamlConfigurationService.class );
    protected final static ObjectMapper mapper = ObjectMapperRegistry.getYamlMapper();
    
    protected final Logger logger = LoggerFactory.getLogger( getClass() );
    protected final AsyncEventBus configurationEvents;
    
    public AbstractYamlConfigurationService(
            AsyncEventBus configurationEvents ) {
        this.configurationEvents = configurationEvents;
    }

    @Override
    public <T extends Configuration> Configuration getConfiguration( Class<T> clazz ) throws IOException {
        Preconditions.checkNotNull( clazz , "Requested configuration class cannot be null." );
        ConfigurationKey key = getConfigurationKey( clazz );
                
        if( key==null || StringUtils.isBlank( key.getId() ) ) {
            throw new InvalidParameterException( "Configuration id for class " + clazz.getName() + " cannot be blank or null" );
        }
        
        try {
            return mapper.readValue( 
                   Preconditions.checkNotNull( fetchConfiguration( key ) , "Configuration cannot be null" ) , 
                   clazz );
        } catch (JsonParseException|JsonMappingException e) {
            logger.error("Invalid YAML configuration file for class " + clazz.getName() );
            return null;
        } 
    }

    @Override
    public <T extends Configuration> void setConfiguration(T configuration) {
        try {
            persistConfiguration( configuration.getKey() , mapper.writeValueAsString( configuration ) );
        } catch(IOException e ) {
            logger.error( "Failed to persist configuration {}",configuration);
        } finally {
            configurationEvents.post( configuration );
        }
    }

    @Override
    public void subscribe(Object subscriber) {
        configurationEvents.register( subscriber );
    }
    
    /**
     * Static method for loading a configuration before the service has been instantiated.  This is useful for bootstrapping an application context
     * or a database connection, which the Configuration service may depend on. The configuration class must have a static {@code getKey()} method 
     * that will be used as the name of the resource containing the YAML configuration.   
     * @param clazz - The configuration class to load.  
     * @return The configuration if it can successfully loaded, null otherwise.
     */
    public static <T extends Configuration> T loadConfiguration( Class<T> clazz ) {
        ConfigurationKey key = getConfigurationKey( clazz );
        
        if( key == null ) {
            LOGGER.error("Unable to load key for configuration class {}" , clazz.getName() );
            return null; 
        }
        
        return loadConfigurationFromResource( key, clazz );
    }
    
    protected static <T extends Configuration> T loadConfigurationFromResource( ConfigurationKey key , Class<T> clazz ) {
        T s = null;
        
        try {   
            s = mapper.readValue( RhizomeUtils.loadResourceToString( key.getId() ) , clazz );
        } catch (IOException e) {
            LOGGER.error("Failed to load default configuration for " + key.getId() , e );
        }

        return s;
    }
    
    protected static ConfigurationKey getConfigurationKey( Class<?> clazz ) {
        /*
         * This requires a static method called key on the class. Unfortunately, in Java 7 it cannot be enforced.
         */
        try {
            Method keyGetter = clazz.getMethod( "key" );
            if( keyGetter == null ) {
                throw new InvalidParameterException( clazz.getName() + " is missing required static method key()." );
            }
            return (ConfigurationKey) keyGetter.invoke( null );
        } catch( MethodNotFoundException nfe ) { 
            LOGGER.error( clazz.getName() + " is missing required static method key()." , nfe );
            return null;
        } catch ( Exception e ) {
          LOGGER.error("Unable to determine configuration id for class " + clazz.getName() , e );
          return null;
        }
    }
    
    protected abstract @Nullable String fetchConfiguration( ConfigurationKey key );
    protected abstract void persistConfiguration( ConfigurationKey key , String configurationYaml );
}
