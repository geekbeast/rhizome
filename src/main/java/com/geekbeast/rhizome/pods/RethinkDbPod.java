package com.geekbeast.rhizome.pods;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import com.dkhenry.RethinkDB.RqlConnection;
import com.geekbeast.rhizome.configuration.ConfigurationConstants;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey;
import com.geekbeast.rhizome.configuration.rethinkdb.RethinkDbConfiguration;
import com.kryptnostic.rhizome.mappers.Mappers;
import com.kryptnostic.rhizome.mappers.keys.ConfigurationKeyMapper;
import com.kryptnostic.rhizome.mapstores.rethinkdb.RethinkDbBaseMapStoreAlternateDriver;
import com.kryptnostic.rhizome.pooling.rethinkdb.RethinkDbAlternateDriverClientPool;

@Configuration
@Profile( "rethinkdb" )
public class RethinkDbPod {
    private static final Logger               logger        = LoggerFactory.getLogger( RethinkDbPod.class );
    private static final RhizomeConfiguration configuration = ConfigurationPod.getRhizomeConfiguration();

    @Bean
    public RethinkDbConfiguration rethinkDbConfiguration() {
        if ( configuration.getRethinkDbConfiguration().isPresent() ) {
            return configuration.getRethinkDbConfiguration().get();
        }
        logger.error( "RethinkDB configuration is missing. Please add a RethinkDB configuration to rhizome.yaml" );
        return null;
    }

    @Bean
    @Scope(
        value = ConfigurableBeanFactory.SCOPE_PROTOTYPE )
    public RqlConnection rethinkConnection() {
        return rethinkDbClientPool().acquire();
    }

    @Bean
    public RethinkDbAlternateDriverClientPool rethinkDbClientPool() {
        return new RethinkDbAlternateDriverClientPool( rethinkDbConfiguration() );
    }

    @Bean
    public RethinkDbBaseMapStoreAlternateDriver<ConfigurationKey, String> configurationMapStore() {
        RethinkDbConfiguration config = rethinkDbConfiguration();
        if ( config != null ) {
            String configurationKeyspace = "configurations";
            return new RethinkDbBaseMapStoreAlternateDriver<ConfigurationKey, String>(
                    rethinkDbClientPool(),
                    ConfigurationConstants.HZ.MAPS.CONFIGURATION,
                    "kryptnostic",
                    configurationKeyspace,
                    new ConfigurationKeyMapper(),
                    Mappers.newStringMapper() ) {

                @Override
                public ConfigurationKey generateTestKey() {
                    return new SimpleConfigurationKey( "com.kryptnostic.test" );
                }

                @Override
                public String generateTestValue() throws Exception {
                    return RandomStringUtils.randomAlphanumeric( 10 );
                }
            };
        }
        return null;
    }
}
