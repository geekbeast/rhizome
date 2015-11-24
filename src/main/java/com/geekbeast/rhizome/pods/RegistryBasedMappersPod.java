package com.geekbeast.rhizome.pods;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Maps;
import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

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

    @Inject
    public void registerValueMappers( Set<SelfRegisteringValueMapper<?>> valueMappers ) {
        for ( SelfRegisteringValueMapper<?> mapper : valueMappers ) {
            valueMapperRegistry.put( mapper.getClazz(), mapper );
        }
    }

    @Inject
    public void registerKeyMappers( Set<SelfRegisteringKeyMapper<?>> keyMappers ) {
        for ( SelfRegisteringKeyMapper<?> mapper : keyMappers ) {
            keyMapperRegistry.put( mapper.getClazz(), mapper );
        }
    }

    @Bean
    public SelfRegisteringKeyMapper<?> noopKM() {
        return new SelfRegisteringKeyMapper<Void>() {

            @Override
            public String fromKey( Void key ) throws MappingException {
                return null;
            }

            @Override
            public Void toKey( String value ) throws MappingException {
                return null;
            }

            @Override
            public Class<Void> getClazz() {
                return Void.class;
            }
        };
    }

    @Bean
    public SelfRegisteringValueMapper<?> noopVM() {
        return new SelfRegisteringValueMapper<Void>() {

            @Override
            public byte[] toBytes( Void value ) throws MappingException {
                return null;
            }

            @Override
            public Void fromBytes( byte[] data ) throws MappingException {
                return null;
            }

            @Override
            public Class<Void> getClazz() {
                return Void.class;
            }
        };
    }

}
