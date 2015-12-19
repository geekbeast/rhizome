package com.geekbeast.rhizome.pods;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Maps;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;

@Configuration
public class RegistryBasedMappersPod {
    private static final ConcurrentMap<Class<?>, SelfRegisteringKeyMapper<?>>   keyMapperRegistry   = Maps.newConcurrentMap();
    private static final ConcurrentMap<Class<?>, SelfRegisteringValueMapper<?>> valueMapperRegistry = Maps.newConcurrentMap();

    public static KeyMapper<?> getKeyMapper( Class<?> clazz ) {
        return keyMapperRegistry.get( clazz );
    }

    public static ValueMapper<?> getValueMapper( Class<?> clazz ) {
        return valueMapperRegistry.get( clazz );
    }

    @Autowired(
        required = false )
    public void registerValueMappers( Set<SelfRegisteringValueMapper<?>> valueMappers ) {
        for ( SelfRegisteringValueMapper<?> mapper : valueMappers ) {
            valueMapperRegistry.put( mapper.getClazz(), mapper );
        }
    }

    @Autowired(
        required = false )
    public void registerKeyMappers( Set<SelfRegisteringKeyMapper<?>> keyMappers ) {
        for ( SelfRegisteringKeyMapper<?> mapper : keyMappers ) {
            keyMapperRegistry.put( mapper.getClazz(), mapper );
        }
    }

}
