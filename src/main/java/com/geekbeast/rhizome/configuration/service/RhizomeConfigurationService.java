package com.geekbeast.rhizome.configuration.service;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.utils.RhizomeUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.AsyncEventBus;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

@Service
public class RhizomeConfigurationService extends AbstractYamlConfigurationService implements MessageListener<Configuration>  {
    private static final Logger logger = LoggerFactory.getLogger( RhizomeConfigurationService.class );
    
    @Resource(name="configurations")
    protected IMap<ConfigurationKey, String> configurations;
    protected final ITopic<Configuration> configurationsTopic;
    
    @Autowired
    public RhizomeConfigurationService( ITopic<Configuration> configurationsTopic , AsyncEventBus configurationEvents ) {
        super( configurationEvents );
        this.configurationsTopic = configurationsTopic;
    }
    
    public <T extends Configuration> T getConfiguration( Class<T> clazz ) {
        ConfigurationKey key = getConfigurationKey( clazz );
        
        if( key == null ) {
            logger.error("Unable to load key for configuration class {}" , clazz.getName() );
            return null;
        }
        
        /*
         * If the Configurations map isn't available load from resource.  Generally, this will be for 
         * Configurations that need to be loaded in order to start. 
         */
        
        T s = null;

        if( configurations != null ) {
            String str = configurations.get( key );
            
            if( StringUtils.isNotBlank( str ) ) {
                try {
                    s = mapper.readValue( str , clazz );
                } catch (IOException e) {
                    logger.error("Failed to read Configuration " + key.getId() , e );
                }
            }
        }
        
        
        /*
         * If we don't have the Configuration available attempt to load a missing Configuration from disk, using
         * the id, which is really the context path.
         */
        
        if( s == null ) {
            s = loadConfigurationFromResource( key, clazz );
        } else { 
            return s;
        }
        
        /*
         * If we successfully read in the Configuration and we have the ability to persist the Configuration do so.  
         */
        
        if( s != null & configurations!=null ) {
            try {
                configurations.put( key , mapper.writeValueAsString( s ) );
            } catch (JsonProcessingException e) {
                logger.error("Unable to marshal value from disk " + key.getId() , e );
            }
        }
        
        return s;
    }
    
    @Override
    public <T extends Configuration> void setConfiguration( T Configuration ) {
        if( configurationsTopic != null ) {
            configurationsTopic.publish( Configuration );
        }
    }
   
    @Override
    public void onMessage(Message<Configuration> message) {
        if ( message.getMessageObject() != null ) {
            logger.debug("Updating Configuration {}" , message.getMessageObject().getKey().getId() );
            super.setConfiguration( message.getMessageObject() );
            logger.debug("Successfully updated Configuration {}" , message.getMessageObject().getKey().getId() );
        }
    } 
    
    @Override
    protected @Nullable String fetchConfiguration(ConfigurationKey key) {
        if( configurations != null ) {
            return Strings.emptyToNull( Preconditions.checkNotNull( configurations.get( key ) ) );
        } else {
            return RhizomeUtils.loadResourceToString( key.getId() );
        }
    }

    @Override
    protected void persistConfiguration(ConfigurationKey key , String configurationYaml) {
        Preconditions.checkNotNull( configurations , "Configuration map is null, cannot persist configurations." )
            .put( key , configurationYaml );
    }
}
