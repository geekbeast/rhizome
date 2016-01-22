package com.kryptnostic.rhizome.pods.hazelcast;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.config.TcpIpConfig;
import com.kryptnostic.rhizome.configuration.RhizomeConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfigurationContainer;
import com.kryptnostic.rhizome.pods.HazelcastPod;

/**
 * This pod provides a basic hazelcast configuration without stream serializers, map stores, or queue stores. If
 * auto-registering of said object is desired use RegistryBasedHazelcastInstanceConfigurationPod.
 *
 * @author Drew Bailey &lt;drew@kryptnostic.com&gt;
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
@Configuration
@Import(HazelcastPod.class)
public class BaseHazelcastInstanceConfigurationPod {
    private static final Lock startupLock = new ReentrantLock();

    @Inject
    protected RhizomeConfiguration configuration;

    @Bean
    public HazelcastConfigurationContainer getHazelcastConfiguration() {
        startupLock.lock();
        return new HazelcastConfigurationContainer(
                getHazelcastServerConfiguration(),
                getHazelcastClientConfiguration() );
    }

    public Config getHazelcastServerConfiguration() {
        Optional<HazelcastConfiguration> maybeConfiguration = configuration.getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        return hzConfiguration.isServer() ? new Config( hzConfiguration.getInstanceName() )
                .setProperty( "hazelcast.logging.type", "slf4j" )
                .setGroupConfig( new GroupConfig( hzConfiguration.getGroup(), hzConfiguration.getPassword() ) )
                .setSerializationConfig( getSerializationConfig() )
                .setMapConfigs( getMapConfigs() )
                .setQueueConfigs( getQueueConfigs() )
                .setNetworkConfig( getNetworkConfig( hzConfiguration ) ) : null;
    }

    public ClientConfig getHazelcastClientConfiguration() {
        Optional<HazelcastConfiguration> maybeConfiguration = configuration.getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        return hzConfiguration.isServer() ? null :
                new ClientConfig()
                        .setNetworkConfig( getClientNetworkConfig( hzConfiguration ) )
                        .setGroupConfig( new GroupConfig( hzConfiguration.getGroup(), hzConfiguration.getPassword() ) )
                        .setSerializationConfig( getSerializationConfig() )
                        .setProperty( "hazelcast.logging.type", "slf4j" );

    }

    @Bean
    public SerializationConfig getSerializationConfig() {
        SerializationConfig config = new SerializationConfig()
                .setSerializerConfigs( getSerializerConfigs() )
                .setAllowUnsafe( true )
                .setUseNativeByteOrder( true );
        return config;
    }

    @Bean
    public static Lock hazelcastStartupLock() {
        return startupLock;
    }
    
    protected static TcpIpConfig getTcpIpConfig( List<String> nodes ) {
        return new TcpIpConfig().setMembers( nodes ).setEnabled( true );
    }

    protected Map<String, MapConfig> getMapConfigs() {
        return ImmutableMap.of();
    }

    protected Map<String, QueueConfig> getQueueConfigs() {
        return ImmutableMap.of();
    }

    protected Collection<SerializerConfig> getSerializerConfigs() {
        return ImmutableList.of();
    }

    protected static ClientNetworkConfig getClientNetworkConfig( HazelcastConfiguration hzConfiguration ) {
        return new ClientNetworkConfig().setAddresses( hzConfiguration.getHazelcastSeedNodes() );
    }

    protected static NetworkConfig getNetworkConfig( HazelcastConfiguration hzConfiguration ) {
        return new NetworkConfig().setPort( hzConfiguration.getPort() ).setJoin(
                getJoinConfig( hzConfiguration.getHazelcastSeedNodes() ) );
    }

    protected static JoinConfig getJoinConfig( List<String> nodes ) {
        return new JoinConfig().setMulticastConfig( new MulticastConfig().setEnabled( false ).setLoopbackModeEnabled(
                false ) ).setTcpIpConfig( getTcpIpConfig( nodes ) );
    }
}
