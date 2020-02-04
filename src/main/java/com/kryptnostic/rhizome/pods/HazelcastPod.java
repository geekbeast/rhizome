package com.kryptnostic.rhizome.pods;

import com.geekbeast.hazelcast.HazelcastClientProvider;
import com.geekbeast.rhizome.async.Synapse;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.AsyncEventBus;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.web.WebFilter;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants;
import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfigurationContainer;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastSessionFilterConfiguration;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;
import com.kryptnostic.rhizome.configuration.service.RhizomeConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Properties;

@Configuration
public class HazelcastPod {
    public static final  String                          CONFIGURATION_UPDATE_TOPIC  = "configuration-update-topic";
    public static final  String                          SESSIONS_MAP_NAME           = "sessions";
    public static final  Logger                          logger                      = LoggerFactory
            .getLogger( HazelcastPod.class );
    private static final String                          HAZELCAST_CONFIGURATION_ERR = "Hazelcast configuration must be present in order to use the HazelcastPod.";
    @Inject
    private              HazelcastConfigurationContainer hazelcastContainerConfiguration;

    /*
     * Hazelcast is finicky and won't properly inject the hazelcast instance unless the resource is called specifically
     * by name Attempting @Inject or @Autowired causes a failure
     */
    @Inject
    private RhizomeConfiguration rhizomeConfiguration;

    @Inject
    private AsyncEventBus        dendrite;

    @Inject
    private SerializationConfig serializationConfig;

    @Inject
    private ConfigurationLoader configurationLoader;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Optional<Config> serverConfig = hazelcastContainerConfiguration.getServerConfig();
        Optional<ClientConfig> clientConfig = hazelcastContainerConfiguration.getClientConfig();
        if ( serverConfig.isPresent() ) {
            return Hazelcast.getOrCreateHazelcastInstance( serverConfig.get() );
        } else if ( clientConfig.isPresent() ) {
            return HazelcastClient.newHazelcastClient( clientConfig.get() );
        } else {
            throw new IllegalStateException( HAZELCAST_CONFIGURATION_ERR );
        }
    }

    @Bean
    public HazelcastClientProvider hazelcastClientProvider() {
        return new HazelcastClientProvider( rhizomeConfiguration.getHazelcastClients().orElseGet( ImmutableMap::of ), serializationConfig );
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
    public Synapse synapse() {
        return new Synapse( axon(), dendrite );
    }

    @Bean
    public ITopic<?> axon() {
        HazelcastInstance hazelcast = hazelcastInstance();
        return hazelcast.getReliableTopic( Topic.RHIZOME_AXON.name() );
    }

    @Bean
    public ITopic<Object> configTopic() {
        return hazelcastInstance().getReliableTopic( Topic.RHIZOME_CONFIGURATION_UPDATES.name() );
    }

    @Bean
    public ConfigurationService configurationService() {
        return new RhizomeConfigurationService( configTopic(), configurations(), configurationLoader, dendrite );
    }

    @Bean
    public Properties hazelcastSessionFilterProperties() {
        if ( !rhizomeConfiguration.isSessionClusteringEnabled() ) {
            return null;
        }
        HazelcastSessionFilterConfiguration filterConfig = rhizomeConfiguration.getHazelcastSessionFilterConfiguration()
                .orElse( null );
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
        if ( !rhizomeConfiguration.isSessionClusteringEnabled() ) {
            return null;
        }
        return new WebFilter( hazelcastSessionFilterProperties() );
    }

    public static enum Topic {
        RHIZOME_CONFIGURATION_UPDATES,
        RHIZOME_AXON
    }

}
