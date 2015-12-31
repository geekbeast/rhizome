package com.geekbeast.rhizome.pods;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;

@Configuration
public class RegistryBasedMappersPod {
    private static final ConcurrentMap<Class<?>, SelfRegisteringKeyMapper<?>>   keyMapperRegistry   = Maps
                                                                                                            .newConcurrentMap();
    private static final ConcurrentMap<Class<?>, SelfRegisteringValueMapper<?>> valueMapperRegistry = Maps
                                                                                                            .newConcurrentMap();

    @Nonnull
    public <T> SelfRegisteringKeyMapper<T> getKeyMapper( @Nonnull Class<T> clazz ) {
        @SuppressWarnings( "unchecked" )
        SelfRegisteringKeyMapper<T> selfRegisteringKeyMapper = (SelfRegisteringKeyMapper<T>) keyMapperRegistry
                .get( Preconditions.checkNotNull( clazz, "Cannot retrieve value mapper for null class." ) );
        return selfRegisteringKeyMapper;
    }

    @Nonnull
    public <T> SelfRegisteringValueMapper<T> getValueMapper( @Nonnull Class<T> clazz ) {
        @SuppressWarnings( "unchecked" )
        SelfRegisteringValueMapper<T> selfRegisteringValueMapper = (SelfRegisteringValueMapper<T>) valueMapperRegistry
                .get( Preconditions.checkNotNull( clazz, "Cannot retrieve value mapper for null class." ) );
        return selfRegisteringValueMapper;
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
