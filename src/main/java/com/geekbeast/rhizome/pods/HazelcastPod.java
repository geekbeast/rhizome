package com.geekbeast.rhizome.pods;

import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.ConfigurationConstants;
import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.hazelcast.HazelcastSessionFilterConfiguration;
import com.geekbeast.rhizome.configuration.service.ConfigurationService;
import com.geekbeast.rhizome.configuration.service.RhizomeConfigurationService;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.web.WebFilter;

@Configuration
public class HazelcastPod {
    public static final Logger   logger                     = LoggerFactory.getLogger( HazelcastPod.class );
    public static final String   SESSIONS_MAP_NAME          = "sessions";
    public static final String   CONFIGURATION_UPDATE_TOPIC = "configuration-update-topic";

    /*
     * Hazelcast is finicky and won't properly inject the hazelcast instance unless the resource is called specifically
     * by name Attempting @Inject or @Autowired causes a failure
     */

    @Inject
    private Config               config;

    @Inject
    private RhizomeConfiguration configuration;

    @Inject
    private AsyncEventBus        configurationUpdates;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.getOrCreateHazelcastInstance( Preconditions.checkNotNull(
                config,
                "Hazelcast configuration must be present in order to use the HazelcastPod." ) );
    }

    @Bean
    public IMap<ConfigurationKey, String> configurations() {
        return hazelcastInstance().getMap( ConfigurationConstants.HZ.MAPS.CONFIGURATION );
    }

    @Bean
    public IMap<String, Object> sessions() {
        return hazelcastInstance().getMap( SESSIONS_MAP_NAME );
    }

    @Bean
    public ITopic<com.geekbeast.rhizome.configuration.Configuration> configTopic() {
        return hazelcastInstance().getTopic( CONFIGURATION_UPDATE_TOPIC );
    }

    @Bean
    public ConfigurationService configurationService() {
        return new RhizomeConfigurationService( configurations(), configTopic(), configurationUpdates );
    }

    @Bean
    public Properties hazelcastSessionFilterProperties() {
        if ( !configuration.isSessionClusteringEnabled() ) {
            return null;
        }
        HazelcastSessionFilterConfiguration filterConfig = configuration.getHazelcastSessionFilterConfiguration()
                .orNull();
        if ( filterConfig == null ) {
            return null;
        }

        Properties hsfProps = new Properties();
        hsfProps.put(
                HazelcastSessionFilterConfiguration.CLIENT_CONFIG_LOCATION_PROPERTY,
                filterConfig.getClientConfigLocation() );
        hsfProps.put(
                HazelcastSessionFilterConfiguration.COOKIE_HTTP_ONLY_PROPERTY,
                filterConfig.getClientConfigLocation() );
        hsfProps.put( HazelcastSessionFilterConfiguration.COOKIE_NAME_PROPERTY, filterConfig.getCookieName() );
        hsfProps.put( HazelcastSessionFilterConfiguration.MAP_NAME_PROPERTY, filterConfig.getMapName() );
        hsfProps.put( HazelcastSessionFilterConfiguration.INSTANCE_NAME_PROPERTY, filterConfig.getInstanceName() );
        hsfProps.put( HazelcastSessionFilterConfiguration.COOKIE_SECURE_PROPERTY, filterConfig.isCookieSecure() );
        hsfProps.put( HazelcastSessionFilterConfiguration.DEBUG_PROPERTY, filterConfig.isDebug() );
        hsfProps.put(
                HazelcastSessionFilterConfiguration.SHUTDOWN_ON_DESTROY_PROPERTY,
                filterConfig.isShutdownOnDestroy() );
        hsfProps.put( HazelcastSessionFilterConfiguration.STICKY_SESSION_PROPERTY, filterConfig.isStickySession() );
        hsfProps.put( HazelcastSessionFilterConfiguration.USE_CLIENT_PROPERTY, filterConfig.useClient() );
        return hsfProps;
    }

    @Bean
    public WebFilter hazelcastSessionFilter() {
        if ( !configuration.isSessionClusteringEnabled() ) {
            return null;
        }
        return new WebFilter( hazelcastSessionFilterProperties() );
    }

}
