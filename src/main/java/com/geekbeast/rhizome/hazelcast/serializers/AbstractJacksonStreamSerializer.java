package com.geekbeast.rhizome.hazelcast.serializers;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.geekbeast.rhizome.pods.hazelcast.RegistryBasedHazelcastInstanceConfigurationPod;
import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

public abstract class AbstractJacksonStreamSerializer<T> implements SelfRegisteringStreamSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T>     clazz;

    protected AbstractJacksonStreamSerializer( Class<T> clazz, ObjectMapper mapper ) {
        this.clazz = clazz;
        this.mapper = mapper;
        RegistryBasedHazelcastInstanceConfigurationPod.register( clazz, this );
    }

    @Override
    public void destroy() {/* No-Op */}

    @Override
    public void write( ObjectDataOutput out, T object ) throws IOException {
        out.writeByteArray( mapper.writeValueAsBytes( object ) );
    }

    @Override
    public T read( ObjectDataInput in ) throws IOException {
        return mapper.readValue( in.readByteArray(), clazz );
    }

    @Override
    public Class<T> getClazz() {
        return clazz;
    }
}
