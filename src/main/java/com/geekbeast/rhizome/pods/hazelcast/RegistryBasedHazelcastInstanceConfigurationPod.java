package com.geekbeast.rhizome.pods.hazelcast;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.Serializer;
import com.kryptnostic.rhizome.mapstores.SelfRegisteringMapStore;

public class RegistryBasedHazelcastInstanceConfigurationPod extends BaseHazelcastInstanceConfigurationPod {
    private static final ConcurrentMap<Class<?>, Serializer>                  serializerRegistry = Maps.newConcurrentMap();
    private static final ConcurrentMap<String, SelfRegisteringMapStore<?, ?>> mapRegistry        = Maps.newConcurrentMap();

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

    public static void register( String mapName, SelfRegisteringMapStore<?, ?> mapStore ) {
        Preconditions.checkArgument( mapRegistry.putIfAbsent( mapName, mapStore ) == null, "Map already registered" );
        ;
    }

    public static void register( Class<?> hzSerializableClass, Serializer serializer ) {
        serializerRegistry.put( hzSerializableClass, serializer );
    }
}
