package com.kryptnostic.rhizome.pods;

import java.util.Map;

import javax.inject.Inject;

import jersey.repackaged.com.google.common.collect.Maps;

import org.springframework.context.annotation.Bean;

import com.google.common.eventbus.AsyncEventBus;
import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.service.AbstractYamlConfigurationService;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

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
