package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class SetStreamSerializers {

    public static <T> void serialize( ObjectDataOutput out , Set<T> elements, IoPerformingConsumer<T> c ) throws IOException {
        out.writeInt( elements.size() );
        for( T elem :  elements ) {
            c.accept( elem );
        }
    }

    public static <T> Set<T> deserialize( ObjectDataInput in, IoPerformingFunction<ObjectDataInput, T> f ) throws IOException {
        int size = in.readInt();
        return deserialize( in, Sets.newHashSetWithExpectedSize( size ), size, f );
    }

    public static <T> Set<T> deserializeOrdered( ObjectDataInput in, IoPerformingFunction<ObjectDataInput, T> f ) throws IOException {
        int size = in.readInt();
        return deserialize( in, Sets.newLinkedHashSetWithExpectedSize( size ), size, f );
    }

    public static <T> Set<T> deserialize( ObjectDataInput in, Set<T> set, int size, IoPerformingFunction<ObjectDataInput, T> f ) throws IOException {
        for ( int i = 0; i < size; ++i ) {
            T elem = f.apply( in );
            if ( elem != null ) {
                set.add( elem );
            }
        }
        return set;
    }
}
