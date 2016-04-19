package com.kryptnostic.rhizome.serializers;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

public abstract class NoContentsStreamSerializer<T> implements SelfRegisteringStreamSerializer<T> {

    private Class<T> clazz;

    public NoContentsStreamSerializer( Class<T> clazz ) {
        this.clazz = clazz;
    }

    @Override
    public void write( ObjectDataOutput out, T object ) throws IOException { /* no-op */ }

    @Override
    public T read( ObjectDataInput in ) throws IOException {
        try {
            return clazz.newInstance();
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new IOException( e );
        }
    }

    @Override
    public void destroy() { /* no-op */ }

    @Override
    public Class<T> getClazz() {
        return clazz;
    }

}
