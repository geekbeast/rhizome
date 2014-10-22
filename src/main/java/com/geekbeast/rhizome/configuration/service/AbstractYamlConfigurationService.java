package com.geekbeast.rhizome.configuration.service;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbeast.rhizome.configuration.Configuration;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.registries.ObjectMapperRegistry;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;

/**
 * Abstract class for loading YAML format configuration classes that handles low level resource reading, validation, and
 * update publishing.
 * 
 * @author Matthew Tamayo-Rios
 */
// TODO: Add hibernate validation
public abstract class AbstractYamlConfigurationService implements ConfigurationService {
    protected final static ObjectMapper mapper = ObjectMapperRegistry.getYamlMapper();

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final AsyncEventBus configurationEvents;

    public AbstractYamlConfigurationService(AsyncEventBus configurationEvents) {
        this.configurationEvents = configurationEvents;
    }

    @Override
    public <T extends Configuration> T getConfiguration(Class<T> clazz) throws IOException {
        Preconditions.checkNotNull(clazz, "Requested configuration class cannot be null.");
        ConfigurationKey key = ConfigurationService.StaticLoader.getConfigurationKey(clazz);
        Preconditions.checkState(key != null && StringUtils.isBlank(key.getUri()), "Configuration id for class "
                + clazz.getName() + " cannot be blank or null");

        try {
            return mapper.readValue(
                    Preconditions.checkNotNull(fetchConfiguration(key), "Configuration cannot be null"), clazz);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error("Invalid YAML configuration file for class " + clazz.getName());
            return null;
        }
    }

    @Override
    public <T extends Configuration> void setConfiguration(T configuration) {
        try {
            persistConfiguration(configuration.getKey(), mapper.writeValueAsString(configuration));
            post(configuration);
        } catch (IOException e) {
            logger.error("Failed to persist configuration {}", configuration);
        }
    }

    @Override
    public void registerModule(Module module) {
        mapper.registerModule(module);
    }
    
    @Override
    public void subscribe(Object subscriber) {
        configurationEvents.register(subscriber);
    }

    protected void post(Configuration configuration) {
        configurationEvents.post(configuration);
    }

    protected abstract @Nullable String fetchConfiguration(ConfigurationKey key);

    protected abstract void persistConfiguration(ConfigurationKey key, String configurationYaml);
}
