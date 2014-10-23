package com.geekbeast.rhizome.pods;

import java.util.Map;

import javax.inject.Inject;

import jersey.repackaged.com.google.common.collect.Maps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.service.AbstractYamlConfigurationService;
import com.geekbeast.rhizome.configuration.service.ConfigurationService;
import com.google.common.eventbus.AsyncEventBus;

@Configuration
public class InMemoryConfigurationServicePod {
    @Inject
    AsyncEventBus eventBus;

    @Bean
    public ConfigurationService configurationService() {
        return new AbstractYamlConfigurationService( eventBus ) {
            private Map<ConfigurationKey, String> map = Maps.newHashMap();

            @Override
            protected void persistConfiguration( ConfigurationKey key, String configurationYaml ) {
                map.put( key, configurationYaml );
            }

            @Override
            protected String fetchConfiguration( ConfigurationKey key ) {
                return map.get( key );
            }
        };
    }
}
