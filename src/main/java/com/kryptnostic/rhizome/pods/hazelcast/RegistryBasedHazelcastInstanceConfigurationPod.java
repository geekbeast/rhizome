package com.kryptnostic.rhizome.pods.hazelcast;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.Serializer;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringMapStore;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringQueueStore;

@Configuration
public class RegistryBasedHazelcastInstanceConfigurationPod extends BaseHazelcastInstanceConfigurationPod {
    private static final Logger                                               logger             = LoggerFactory
                                                                                                         .getLogger( RegistryBasedHazelcastInstanceConfigurationPod.class );
    private static final ConcurrentMap<Class<?>, Serializer>                  serializerRegistry = Maps
                                                                                                         .newConcurrentMap();
    private static final ConcurrentMap<String, SelfRegisteringMapStore<?, ?>> mapRegistry        = Maps
                                                                                                         .newConcurrentMap();
    private static final ConcurrentMap<String, SelfRegisteringQueueStore<?>>  queueRegistry      = Maps
                                                                                                         .newConcurrentMap();

    @Override
    protected Collection<SerializerConfig> getSerializerConfigs() {
        return Collections2.transform( serializerRegistry.entrySet(), e -> {
            return new SerializerConfig().setTypeClass( e.getKey() ).setImplementation( e.getValue() );
        } );
    }

    @Override
    protected Map<String, MapConfig> getMapConfigs() {
        return Maps.transformEntries( mapRegistry, ( k, v ) -> {
            return v.getMapConfig();
        } );
    }

    @Override
    protected Map<String, QueueConfig> getQueueConfigs() {
        return Maps.transformEntries( queueRegistry, ( k, v ) -> {
            return v.getQueueConfig();
        } );
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

            register( s.getMapConfig().getName(), s );
        }
    }

    @Autowired(
        required = false )
    public void registerQueueStores( Set<SelfRegisteringQueueStore<?>> queueStores ) {
        if ( queueStores.isEmpty() ) {
            logger.warn( "No queue stores were configured." );
        }
        for ( SelfRegisteringQueueStore<?> s : queueStores ) {
            queueRegistry.put( s.getQueueConfig().getName(), s );
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

    public static void register( String queueName, SelfRegisteringQueueStore<?> queueStore ) {
        Preconditions.checkNotNull( queueStore, "Cannot register null queue-store." );
        queueRegistry.put( queueName, queueStore );
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
