package com.kryptnostic.rhizome.pods.hazelcast;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.Serializer;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringMapStore;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.openlattice.hazelcast.pods.QueueConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryBasedHazelcastInstanceConfigurationPod extends BaseHazelcastInstanceConfigurationPod {
    private static final Logger                                               logger             = LoggerFactory
            .getLogger( RegistryBasedHazelcastInstanceConfigurationPod.class );
    private static final ConcurrentMap<Class<?>, Serializer>                  serializerRegistry = Maps
            .newConcurrentMap();
    private static final ConcurrentMap<String, SelfRegisteringMapStore<?, ?>> mapRegistry        = Maps
            .newConcurrentMap();

    private static final Set<QueueConfigurer> queueConfigurers = Sets.newHashSet();
    private static final Set<NearCacheConfig> nearCacheConfigs = Sets.newHashSet();

    @Override
    protected Collection<SerializerConfig> serializerConfigs() {
        final Multiset<Integer> typeIds = HashMultiset.create();

        Set<SerializerConfig> configs = serializerRegistry.entrySet()
                .stream()
                .peek( e -> typeIds.add( e.getValue().getTypeId() ) )
                .map( e -> new SerializerConfig().setTypeClass( e.getKey() ).setImplementation( e.getValue() ) )
                .collect( Collectors.toSet() );
        logger.info( "Detected the following serializer configurations at startup {}.", configs );
        typeIds.entrySet()
                .stream()
                .filter( e -> e.getCount() > 1 )
                .forEach( e -> logger.warn( "Found {} duplicate serializers for type id: {}",
                        e.getCount(),
                        e.getElement() ) );

        return configs;
    }

    @Override
    protected Map<String, MapConfig> mapConfigs() {
        return Maps.transformEntries( mapRegistry, ( k, v ) -> v.getMapConfig() );
    }

    @Override
    protected Map<String, QueueConfig> queueConfigs( Map<String, QueueConfig> queueConfigs ) {
        Map<String, QueueConfig> configs = Maps.newHashMap( queueConfigs );
        queueConfigurers.forEach( configurer -> {
            final QueueConfig config = configs
                    .computeIfAbsent( configurer.getQueueName(), k -> new QueueConfig( k ).setBackupCount( 1 ) );
            configurer.configure( config );
        } );
        return configs;
    }

    @Override
    protected Map<String, NearCacheConfig> nearCacheConfigs() {
        return nearCacheConfigs
                .stream()
                .collect( Collectors.toMap( NearCacheConfig::getName, Function.identity() ) );
    }

    /*
     * The following three methods use @Autowired instead of @Inject, since that functionality is not provided by the
     * spec :-/
     */

    @Autowired(
            required = false )
    public void registerMapStores( Set<SelfRegisteringMapStore<?, ?>> mapStores ) {
        if ( mapStores.isEmpty() ) {
            logger.warn( "No map stores were configured." );
        }
        for ( SelfRegisteringMapStore<?, ?> s : mapStores ) {
            //This ensures that Hazelcast will use the byte-code re-written beans instead of the mapstores directly
            //The metrics enabled flag can be overriden for debugging particular mapstores if stacktraces are too dirty
            if ( s.isMetricsEnabled() ) {
                s.getMapStoreConfig().setImplementation( s );
            }
            register( s.getMapConfig().getName(), s );
        }
    }

    @Autowired(
            required = false )
    public void register( Set<SelfRegisteringStreamSerializer<?>> serializers ) {
        if ( serializers.isEmpty() ) {
            logger.warn( "No serializers were configured." );
        }
        for ( SelfRegisteringStreamSerializer<?> s : serializers ) {
            serializerRegistry.put( s.getClazz(), s );
        }
    }

    @Autowired( required = false )
    public void configureNearCaches( Set<NearCacheConfig> nearCacheConfigs ) {
        if ( nearCacheConfigs.isEmpty() ) {
            logger.info( "No near cache configurers detected." );
        }

        this.nearCacheConfigs.addAll( nearCacheConfigs );
    }

    @Autowired(
            required = false )
    public void configureQueues( Set<QueueConfigurer> queueConfigurers ) {
        if ( queueConfigurers.isEmpty() ) {
            logger.info( "No queue configurers detected." );
        }
        this.queueConfigurers.addAll( queueConfigurers );
    }

    public static void register( String mapName, SelfRegisteringMapStore<?, ?> mapStore ) {
        Preconditions.checkNotNull( mapStore, "Cannot register null map-store." );
        mapRegistry.put( mapName, mapStore );
    }

    public static void register( Class<?> hzSerializableClass, Serializer serializer ) {
        Preconditions.checkNotNull( serializer, "Cannot register null serializer." );
        serializerRegistry.put( hzSerializableClass, serializer );
    }

}
