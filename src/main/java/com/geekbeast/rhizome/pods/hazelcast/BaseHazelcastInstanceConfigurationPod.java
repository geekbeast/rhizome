package com.geekbeast.rhizome.pods.hazelcast;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import jersey.repackaged.com.google.common.base.Preconditions;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.config.TcpIpConfig;

@Configuration
public class BaseHazelcastInstanceConfigurationPod {

    @Inject
    protected RhizomeConfiguration configuration;

    @Bean
    public Config getHazelcastConfiguration() {
        Optional<HazelcastConfiguration> maybeConfiguration = configuration.getHazelcastConfiguration();
        Preconditions.checkArgument(
                maybeConfiguration.isPresent(),
                "Hazelcast Configuration must be present to build hazelcast instance configuration." );
        HazelcastConfiguration hzConfiguration = maybeConfiguration.get();
        Config config = new Config( hzConfiguration.getInstanceName() )
                .setGroupConfig( new GroupConfig( hzConfiguration.getGroup(), hzConfiguration.getPassword() ) )
                .setSerializationConfig( new SerializationConfig().setSerializerConfigs( getSerializerConfigs() ) )
                .setMapConfigs( getMapConfigs() ).setNetworkConfig( getNetworkConfig( hzConfiguration ) );
        return config;
    }

    protected NetworkConfig getNetworkConfig( HazelcastConfiguration hzConfiguration ) {
        return new NetworkConfig().setPort( hzConfiguration.getPort() ).setJoin(
                getJoinConfig( hzConfiguration.getHazelcastSeedNodes() ) );
    }

    protected JoinConfig getJoinConfig( List<String> nodes ) {
        return new JoinConfig().setMulticastConfig( new MulticastConfig().setEnabled( false ).setLoopbackModeEnabled(
                false ) );
    }

    protected TcpIpConfig getTcpIpConfig( List<String> nodes ) {
        return new TcpIpConfig().setMembers( nodes ).setEnabled( true );
    }

    protected Map<String, MapConfig> getMapConfigs() {
        return ImmutableMap.of();
    }

    protected Collection<SerializerConfig> getSerializerConfigs() {
        return ImmutableList.of();
    }

}
