package com.kryptnostic.rhizome.serializers;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public abstract class AbstractJacksonStreamSerializer<T> implements StreamSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T>     clazz;

    protected AbstractJacksonStreamSerializer( Class<T> clazz, ObjectMapper mapper ) {
        this.clazz = clazz;
        this.mapper = mapper;
    }

    @Override
    public void destroy() {}

    @Override
    public void write( ObjectDataOutput out, T object ) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes( object );
        out.writeInt( bytes.length );
        out.write( bytes );
    }

    @Override
    public T read( ObjectDataInput in ) throws IOException {
        byte[] bytes = new byte[ in.readInt() ];
        in.readFully( bytes );
        return mapper.readValue( bytes, clazz );
    }
}
