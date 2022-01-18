package com.geekbeast.rhizome.configuration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.pods.ConfigurationLoader;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.AsyncEventBus;

import com.hazelcast.topic.Message;

import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.MessageListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;

public class RhizomeConfigurationService extends AbstractYamlConfigurationService implements
        MessageListener<Object> {
    private static final Logger                    logger = LoggerFactory
            .getLogger( RhizomeConfigurationService.class );

    protected final IMap<ConfigurationKey, String> configurations;
    protected final ITopic<Object>                 configurationsTopic;
    protected final ConfigurationLoader            configurationLoader;

    public RhizomeConfigurationService(
            ITopic<Object> configurationsTopic,
            IMap<ConfigurationKey, String> configurations,
            ConfigurationLoader configurationLoader,
            AsyncEventBus configurationUpdates ) {
        super( configurationUpdates );
        this.configurationLoader = configurationLoader;
        this.configurationsTopic = configurationsTopic;
        this.configurations = Preconditions.checkNotNull( configurations, "Configurations map cannot be null." );
        configurationsTopic.addMessageListener( this );
        logger.info("Finished launching of Rhizome Configuration Service!");
    }

    @Override
    public @Nullable <T> T getConfiguration( Class<T> clazz ) {
        ConfigurationKey key = ConfigurationService.StaticLoader.getConfigurationKey( clazz );

        // If we end up with a null key log an error
        if ( key == null ) {
            logger.error( "Unable to load key for configuration class {}", clazz.getName() );
            return null;
        }

        /*
         * If the Configurations map isn't available load from resource. Generally, this will be for Configurations that
         * need to be loaded in order to start.
         */

        T s = null;

        if ( configurations != null ) {
            String str = configurations.get( key );

            if ( StringUtils.isNotBlank( str ) ) {
                try {
                    s = mapper.readValue( str, clazz );
                } catch ( IOException e ) {
                    logger.error( "Failed to read Configuration " + key.getUri(), e );
                }
            }
        }

        /*
         * If we don't have the Configuration available attempt to load a missing Configuration from disk, using the id,
         * which is really the context path.
         */

        if ( s == null ) {
            logger.debug( "Configuration key {} value unavailable. Attempting to load from disk.", key );

            s = configurationLoader.load( clazz );

        } else {
            return s;
        }

        /*
         * If we successfully read in the Configuration and we have the ability to persist the Configuration do so.
         */

        if ( s != null & configurations != null ) {
            try {
                configurations.put( key, mapper.writeValueAsString( s ) );
            } catch ( JsonProcessingException e ) {
                logger.error( "Unable to marshal value from disk " + key.getUri(), e );
            }
        }

        return s;
    }

    @Override
    public <T> void setConfiguration( T configuration ) {
        ConfigurationKey key = ConfigurationService.StaticLoader.getConfigurationKey( configuration.getClass() );
        try {
            persistConfiguration( key, mapper.writeValueAsString( configuration ) );
            configurationsTopic.publish( configuration );
        } catch ( JsonProcessingException e ) {
            logger.error( "Unable to set configuration {} = {}", key, configuration, e );
        }
    }

    @Override
    public void onMessage( Message<Object> message ) {
        /*
         * This is a light weight operation, that hands off to the async event bus thread pool. It will trigger a post
         * that asynchronously notifies all subscribers that the configuration has been updated. It will not trigger a
         * re-persist of information.
         */
        if ( message.getMessageObject() != null ) {
            String uri = "";

            if ( logger.isDebugEnabled() ) {
                uri = ConfigurationService.StaticLoader.getConfigurationKey( message.getMessageObject().getClass() )
                        .getUri();
            }

            logger.debug( "Updating Configuration {}", uri );
            post( message.getMessageObject() );
            logger.debug( "Successfully updated Configuration {}", uri );
        }
    }

    @Override
    protected @Nullable String fetchConfiguration( ConfigurationKey key ) {
        return Strings.emptyToNull( configurations.get( key ) );
    }

    @Override
    protected void persistConfiguration( ConfigurationKey key, String configurationYaml ) {
        configurations.put( key, configurationYaml );
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
