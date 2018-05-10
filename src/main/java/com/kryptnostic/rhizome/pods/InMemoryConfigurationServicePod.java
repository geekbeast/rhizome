package com.kryptnostic.rhizome.pods;

import com.google.common.eventbus.AsyncEventBus;
import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.service.AbstractYamlConfigurationService;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.springframework.context.annotation.Bean;

public class InMemoryConfigurationServicePod {
    @Inject
    AsyncEventBus eventBus;

    @Bean
    public ConfigurationService configurationService() {
        return new AbstractYamlConfigurationService( eventBus ) {
            private Map<ConfigurationKey, String> map = new HashMap<>();

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
