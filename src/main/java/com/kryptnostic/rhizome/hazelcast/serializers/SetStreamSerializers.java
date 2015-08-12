package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

import jersey.repackaged.com.google.common.collect.Sets;

import com.google.common.base.Function;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class SetStreamSerializers {

    public static <T> void serialize( ObjectDataOutput out , Set<T> elements, Consumer<T> c ) throws IOException {
        out.writeInt( elements.size() );
        elements.forEach( c );
    }

    public static <T> Set<T> deserialize( ObjectDataInput in, Function<ObjectDataInput, T> f ) throws IOException {
        int size = in.readInt();
        Set<T> s = Sets.newHashSetWithExpectedSize( size );
        for ( int i = 0; i < size; ++i ) {
            T elem = f.apply( in );
            if ( elem != null ) {
                s.add( elem );
            }
        }
        return s;
    }
}
