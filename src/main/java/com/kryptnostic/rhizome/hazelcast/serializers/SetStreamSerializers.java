package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.hazelcast.objects.OrderedUUIDSet;
import com.kryptnostic.rhizome.hazelcast.objects.UUIDSet;

public class SetStreamSerializers {

    public static <T> void serialize( ObjectDataOutput out, Set<T> elements, IoPerformingConsumer<T> c ) throws IOException {
        out.writeInt( elements.size() );
        for( T elem :  elements ) {
            c.accept( elem );
        }
    }

    public static void fastUUIDSetSerialize( ObjectDataOutput out, Set<UUID> object ) throws IOException {
        long[] least = new long[ object.size() ];
        long[] most = new long[ object.size() ];
        int i = 0;
        for ( UUID uuid : object ) {
            least[ i ] = uuid.getLeastSignificantBits();
            most[ i ] = uuid.getMostSignificantBits();
            i++;
        }
        out.writeInt( i );
        out.writeLongArray( least );
        out.writeLongArray( most );
    }

    public static OrderedUUIDSet fastOrderedUUIDSetDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        OrderedUUIDSet set = new OrderedUUIDSet( size );
        return (OrderedUUIDSet) processEntries( set, size, in );
    }

    public static UUIDSet fastUUIDSetDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        UUIDSet set = new UUIDSet( size );
        return (UUIDSet) processEntries( set, size, in );
    }

    private static Set<UUID> processEntries( Set<UUID> set, int size, ObjectDataInput in ) throws IOException {
        long[] least = in.readLongArray();
        long[] most = in.readLongArray();
        for ( int i=0; i < size; i++ ) {
            set.add( new UUID( most[ i ], least[ i ] ) );
        }
        return set;
    }

    public static <T> Set<T> deserialize( ObjectDataInput in, IoPerformingFunction<ObjectDataInput, T> f ) throws IOException {
        int size = in.readInt();
        return deserialize( in, Sets.newHashSetWithExpectedSize( size ), size, f );
    }

    public static <T> Set<T> deserialize( ObjectDataInput in, Set<T> set, int size, IoPerformingFunction<ObjectDataInput, T> f )
            throws IOException {
        for ( int i = 0; i < size; ++i ) {
            T elem = f.apply( in );
            if ( elem != null ) {
                set.add( elem );
            }
        }
        return set;
    }
}
