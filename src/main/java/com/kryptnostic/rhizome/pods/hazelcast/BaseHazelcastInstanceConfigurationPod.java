package com.kryptnostic.rhizome.pods.hazelcast;

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
import com.kryptnostic.rhizome.pods.ConfigurationPod;
import com.kryptnostic.rhizome.pods.HazelcastPod;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This pod provides a basic hazelcast configuration without stream serializers, map stores, or queue stores. If
 * auto-registering of said object is desired use RegistryBasedHazelcastInstanceConfigurationPod.
 *
 * @author Drew Bailey &lt;drew@kryptnostic.com&gt;
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
@Configuration
@Import( { HazelcastPod.class, ConfigurationPod.class } )
public class BaseHazelcastInstanceConfigurationPod {
    private static final Logger logger = LoggerFactory.getLogger( BaseHazelcastInstanceConfigurationPod.class );

    @Inject
    protected RhizomeConfiguration rhizomeConfiguration;

    @Bean
    public HazelcastConfigurationContainer getHazelcastConfiguration() {
        return new HazelcastConfigurationContainer(
                getHazelcastServerConfiguration(),
                getHazelcastClientConfiguration() );
    }

    public Config getHazelcastServerConfiguration() {
        Optional<HazelcastConfiguration> maybeConfiguration = rhizomeConfiguration.getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        if ( hzConfiguration.isServer() ) {
            Config config = new Config( hzConfiguration.getInstanceName() );

            config
                    .setProperty( "hazelcast.logging.type", "slf4j" )
                    .setProperty( "hazelcast.slow.operation.detector.stacktrace.logging.enabled", "true" )
                    .setProperty( "hazelcast.map.load.chunk.size","100000" )
                    .setGroupConfig( new GroupConfig( hzConfiguration.getGroup(), hzConfiguration.getPassword() ) )
                    .setSerializationConfig( getSerializationConfig() )
                    .setMapConfigs( getMapConfigs() )
                    .setQueueConfigs( getQueueConfigs( config.getQueueConfig( "default" ) ) )
                    .setNetworkConfig( getNetworkConfig( hzConfiguration ) );

            config.getCPSubsystemConfig().setCPMemberCount( hzConfiguration.getCPMemberCount() );
            return config;
        }
        return null;
    }

    public ClientConfig getHazelcastClientConfiguration() {
        Optional<HazelcastConfiguration> maybeConfiguration = rhizomeConfiguration.getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        SerializationConfig serializationConfig = getSerializationConfig();
        logger.info( "Registering the following serializers: {}", serializationConfig );
        return hzConfiguration.isServer() ? null : new ClientConfig()
                .setNetworkConfig( getClientNetworkConfig( hzConfiguration ) )
                .setGroupConfig( new GroupConfig( hzConfiguration.getGroup(), hzConfiguration.getPassword() ) )
                .setSerializationConfig( serializationConfig )
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

    protected Map<String, MapConfig> getMapConfigs() {
        return ImmutableMap.of();
    }

    protected Map<String, QueueConfig> getQueueConfigs( QueueConfig defaultConfig ) {
        return getQueueConfigs( ImmutableMap.of( "default", defaultConfig ) );
    }

    protected Map<String, QueueConfig> getQueueConfigs( Map<String, QueueConfig> configs ) {
        return ImmutableMap.copyOf( configs );
    }

    protected Collection<SerializerConfig> getSerializerConfigs() {
        return ImmutableList.of();
    }

    protected static TcpIpConfig getTcpIpConfig( List<String> nodes ) {
        return new TcpIpConfig().setMembers( nodes ).setEnabled( true );
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
