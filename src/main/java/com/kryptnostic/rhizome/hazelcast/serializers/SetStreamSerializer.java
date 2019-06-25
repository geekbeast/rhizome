package com.kryptnostic.rhizome.hazelcast.serializers;

import com.hazelcast.nio.ObjectDataInput;

import java.io.IOException;
import java.util.Set;

public abstract class SetStreamSerializer<T extends Set<E>, E> extends CollectionStreamSerializer<T, E> {

    protected SetStreamSerializer( Class<T> clazz ) {
        super( clazz );
    }

    @Override
    public T read( ObjectDataInput in ) throws IOException {
        var size = in.readInt();
        final var obj = newInstanceWithExpectedSize( size );
        for ( int i = 0; i < size; i++ ) {
            obj.add( readSingleElement( in ) );
        }
        return obj;
    }
}
