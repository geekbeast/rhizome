package com.geekbeast.rhizome.pods;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.google.common.eventbus.AsyncEventBus;
import com.geekbeast.rhizome.configuration.service.AbstractYamlConfigurationService;
import com.geekbeast.rhizome.configuration.service.ConfigurationService;
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
