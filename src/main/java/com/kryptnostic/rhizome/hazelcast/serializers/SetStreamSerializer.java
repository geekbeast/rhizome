package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Set;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

public abstract class SetStreamSerializer<T extends Set<E>, E> implements SelfRegisteringStreamSerializer<T> {
    private Class<T> clazz;

    protected SetStreamSerializer( Class<T> clazz ) {
        this.clazz = clazz;
    }

    @Override
    public void write( ObjectDataOutput out, T object ) throws IOException {
        out.writeInt( object.size() );
        for ( E element : object ) {
            writeSingleElement( out, element );
        }
    }

    @Override
    public T read( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        T obj = newInstanceWithExpectedSize( size );
        for ( int i = 0; i < size; ++i ) {
            obj.add( readSingleElement( in ) );
        }
        return obj;
    }

    protected abstract T newInstanceWithExpectedSize( int size );

    protected abstract E readSingleElement( ObjectDataInput in ) throws IOException;

    protected abstract void writeSingleElement( ObjectDataOutput out, E element ) throws IOException;

    @Override
    public void destroy() {}

    @Override
    public Class<T> getClazz() {
        return clazz;
    }

}
