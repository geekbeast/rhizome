package com.geekbeast.rhizome.pods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.kryptnostic.rhizome.mappers.Mappers;
import com.kryptnostic.rhizome.mappers.keys.KeyMappers;
import com.kryptnostic.rhizome.rethinkdb.BaseRethinkDbMapStore;
import com.kryptnostic.rhizome.rethinkdb.DefaultRethinkDbClientPool;
import com.rethinkdb.RethinkDBConnection;

@Configuration
public class RethinkDbPod {
    private static final Logger               logger        = LoggerFactory.getLogger( RethinkDbPod.class );
    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();

    @Bean
    public RethinkDbConfiguration rethinkDbConfiguration() {
        if ( configuration.getRethinkDbConfiguration().isPresent() ) {
            return configuration.getRethinkDbConfiguration().get();
        } else {
            logger.error( "RethinkDB configuration is missing. Please add a RethinkDB configuration to rhizome.yaml" );
            return null;
        }
    }

    @Bean
    @Scope(
        value = ConfigurableBeanFactory.SCOPE_PROTOTYPE )
    public RethinkDBConnection rethinkConnection() {
        return rethinkDbClientPool().acquire();
    }

    @Bean
    public DefaultRethinkDbClientPool rethinkDbClientPool() {
        return new DefaultRethinkDbClientPool( rethinkDbConfiguration() );
    }

    @Bean
    public BaseRethinkDbMapStore<ConfigurationKey, String> configurationMapStore() {
        RethinkDbConfiguration config = rethinkDbConfiguration();
        if ( config != null ) {
            String configurationKeyspace = "configurations";
            return new BaseRethinkDbMapStore<ConfigurationKey, String>(
                    rethinkDbClientPool(),
                    "kryptnostic",
                    configurationKeyspace,
                    KeyMappers.newConfigurationKeyMapper(),
                    Mappers.newStringMapper() );
        }
        return null;
    }
}
