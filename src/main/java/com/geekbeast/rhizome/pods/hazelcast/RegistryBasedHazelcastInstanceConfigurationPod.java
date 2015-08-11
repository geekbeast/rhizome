package com.geekbeast.rhizome.pods.hazelcast;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.hazelcast.HazelcastConfiguration;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
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
import com.hazelcast.nio.serialization.Serializer;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringMapStore;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringQueueStore;

@Configuration
public class RegistryBasedHazelcastInstanceConfigurationPod {
    private static final ConcurrentMap<Class<?>, Serializer>                  serializerRegistry = Maps.newConcurrentMap();
    private static final ConcurrentMap<String, SelfRegisteringMapStore<?, ?>> mapRegistry        = Maps.newConcurrentMap();
    private static final ConcurrentMap<String, SelfRegisteringQueueStore<?>>  queueRegistry      = Maps.newConcurrentMap();

    @Inject
    protected RhizomeConfiguration                                            configuration;

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
                .setMapConfigs( getMapConfigs() ).setNetworkConfig( getNetworkConfig( hzConfiguration ) )
                .setQueueConfigs( getQueueConfigs() );
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

    protected Collection<SerializerConfig> getSerializerConfigs() {
        return Collections2.transform( serializerRegistry.entrySet(), e -> {
            return new SerializerConfig().setTypeClass( e.getKey() ).setImplementation( e.getValue() );
        } );
    }

    protected Map<String, MapConfig> getMapConfigs() {
        return Maps.transformEntries( mapRegistry, ( k, v ) -> {
            return v.getMapConfig();
        } );
    }

    protected Map<String, QueueConfig> getQueueConfigs() {
        return Maps.transformEntries( queueRegistry, ( k, v ) -> {
            return v.getQueueConfig();
        } );
    }

    @Inject
    public void registerMapStores( Set<SelfRegisteringMapStore<?, ?>> mapStores ) {
        for ( SelfRegisteringMapStore<?, ?> s : mapStores ) {
            mapRegistry.put( s.getMapConfig().getName(), s );
        }
    }

    @Inject
    public void registerQueueStores( Set<SelfRegisteringQueueStore<?>> queueStores ) {
        for ( SelfRegisteringQueueStore<?> s : queueStores ) {
            queueRegistry.put( s.getQueueConfig().getName(), s );
        }
    }

    @Inject
    public void register( Set<SelfRegisteringStreamSerializer<?>> serializers ) {
        for ( SelfRegisteringStreamSerializer<?> s : serializers ) {
            serializerRegistry.put( s.getClazz(), s );
        }
    }

    public static void register( String queueName, SelfRegisteringQueueStore<?> queueStore ) {
        queueRegistry.put( queueName, queueStore );
    }

    public static void register( String mapName, SelfRegisteringMapStore<?, ?> mapStore ) {
        mapRegistry.put( mapName, mapStore );
    }

    public static void register( Class<?> hzSerializableClass, Serializer serializer ) {
        serializerRegistry.put( hzSerializableClass, serializer );
    }

    // TODO: Make map and serializer beans as startup will fail if there is not at least one bean of that type.
    @Bean
    public SelfRegisteringQueueStore<?> noopQ() {
        return new SelfRegisteringQueueStore<Void>() {

            @Override
            public void store( Long key, Void value ) {

            }

            @Override
            public void storeAll( Map<Long, Void> map ) {

            }

            @Override
            public void delete( Long key ) {

            }

            @Override
            public void deleteAll( Collection<Long> keys ) {

            }

            @Override
            public Void load( Long key ) {
                return null;
            }

            @Override
            public Map<Long, Void> loadAll( Collection<Long> keys ) {
                return null;
            }

            @Override
            public Set<Long> loadAllKeys() {
                return null;
            }

            @Override
            public QueueConfig getQueueConfig() {
                return new QueueConfig( "noop" );
            }
        };
    }
}
