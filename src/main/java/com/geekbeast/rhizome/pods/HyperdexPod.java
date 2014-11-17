package com.geekbeast.rhizome.pods;

import org.hyperdex.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexConfiguration;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexPreconfigurer;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;
import com.kryptnostic.rhizome.hyperdex.pooling.ResizingHyperdexClientPool;
import com.kryptnostic.rhizome.mapstores.ConfigurationHyperdexMapstore;

@Configuration
public class HyperdexPod {
    private static final Logger               logger        = LoggerFactory.getLogger( HyperdexPod.class );

    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();

    static {
        HyperdexPreconfigurer.configure();
    }

    @Bean
    public HyperdexConfiguration hyperdexConfiguration() {
        if ( configuration.getHyperdexConfiguration().isPresent() ) {
            return configuration.getHyperdexConfiguration().get();
        } else {
            logger.error( "Hyperdex configuration is missing. Please add a hyperdex configuration to rhizome.yaml" );
            return null;
        }
    }

    @Bean
    @Scope(
        value = ConfigurableBeanFactory.SCOPE_PROTOTYPE )
    public Client hyperdexClient() {
        return hyperdexClientPool().acquire();
    }

    @Bean
    public HyperdexClientPool hyperdexClientPool() {
        return new ResizingHyperdexClientPool( hyperdexConfiguration() );
    }

    @Bean
    public ConfigurationHyperdexMapstore configurationMapStore() {
        HyperdexConfiguration hyperdexConfiguration = hyperdexConfiguration();
        Optional<String> configurationKeyspace;
        if ( hyperdexConfiguration != null ) {
            configurationKeyspace = hyperdexConfiguration().getConfigurationKeyspace();
            if ( configurationKeyspace.isPresent() ) {
                return new ConfigurationHyperdexMapstore( configurationKeyspace.get(), hyperdexClientPool() );
            }
        }
        return null;
    }
}
