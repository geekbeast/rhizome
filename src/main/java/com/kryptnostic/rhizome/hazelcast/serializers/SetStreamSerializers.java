package com.kryptnostic.rhizome.hazelcast.serializers;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.openlattice.rhizome.hazelcast.OrderedUUIDSet;
import com.openlattice.rhizome.hazelcast.UUIDSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class SetStreamSerializers {

    public static <T> void serialize( ObjectDataOutput out, Set<T> elements, IoPerformingConsumer<T> c )
            throws IOException {
        out.writeInt( elements.size() );
        for ( T elem : elements ) {
            c.accept( elem );
        }
    }

    /**
     * This method is useful for re-using static serialization helpers to write out set elements.
     */
    public static <T> void serialize(
            ObjectDataOutput out,
            Set<T> elements,
            IoPerformingBiConsumer<ObjectDataOutput, T> c ) throws IOException {
        out.writeInt( elements.size() );
        for ( T elem : elements ) {
            c.accept( out, elem );
        }
    }

    public static <T> void serialize( ObjectDataOutput out, Iterable<T> elements, IoPerformingConsumer<T> c )
            throws IOException {
        // Iterables correctly does collections efficiently.
        out.writeInt( Iterables.size( elements ) );
        for ( T elem : elements ) {
            c.accept( elem );
        }
    }

    public static void fastUUIDSetSerialize( ObjectDataOutput out, Iterable<UUID> object ) throws IOException {
        long[] least = new long[ Iterables.size( object ) ];
        long[] most = new long[ Iterables.size( object ) ];
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

    public static void fastUUIDSetDeserialize( ObjectDataInput in, Set<UUID> out ) throws IOException {
        final int size = in.readInt();
        processEntries( out, size, in );
    }

    public static UUIDSet fastUUIDSetDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        UUIDSet set = new UUIDSet( size );
        return (UUIDSet) processEntries( set, size, in );
    }

    private static Set<UUID> processEntries( Set<UUID> set, int size, ObjectDataInput in ) throws IOException {
        long[] least = in.readLongArray();
        long[] most = in.readLongArray();
        for ( int i = 0; i < size; i++ ) {
            set.add( new UUID( most[ i ], least[ i ] ) );
        }
        return set;
    }

    public static <T> Set<T> deserialize( ObjectDataInput in, IoPerformingFunction<ObjectDataInput, T> f )
            throws IOException {
        int size = in.readInt();
        return deserialize( in, Sets.newHashSetWithExpectedSize( size ), size, f );
    }

    public static <T> LinkedHashSet<T> orderedDeserialize(
            ObjectDataInput in,
            IoPerformingFunction<ObjectDataInput, T> f )
            throws IOException {
        int size = in.readInt();
        return deserialize( in, Sets.newLinkedHashSetWithExpectedSize( size ), size, f );
    }

    public static <T, S extends Set<T>> S deserialize(
            ObjectDataInput in,
            S set,
            int size,
            IoPerformingFunction<ObjectDataInput, T> f )
            throws IOException {
        for ( int i = 0; i < size; ++i ) {
            T elem = f.apply( in );
            if ( elem != null ) {
                set.add( elem );
            }
        }
        return set;
    }

    public static void fastOrderedStringSetSerializeAsArray( ObjectDataOutput out, Set<String> object )
            throws IOException {
        out.writeUTFArray( object.toArray( new String[ 0 ] ) );
    }

    public static void fastStringSetSerialize( ObjectDataOutput out, Iterable<String> object ) throws IOException {
        int size = Iterators.size( object.iterator() );
        out.writeInt( size );
        for ( String item : object ) {
            out.writeUTF( item );
        }
    }

    public static void fastStringSetSerialize( ObjectDataOutput out, Collection<String> object ) throws IOException {
        out.writeInt( object.size() );
        for ( String item : object ) {
            out.writeUTF( item );
        }
    }

    public static Set<String> fastStringSetDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        Set<String> items = Sets.newHashSetWithExpectedSize( size );
        for ( int i = 0; i < size; i++ ) {
            items.add( in.readUTF() );
        }
        return items;
    }

    public static Set<String> fastOrderedStringSetDeserializeAsArray( ObjectDataInput in ) throws IOException {
        final var arr = in.readUTFArray();
        return new LinkedHashSet<>( Arrays.asList( arr ) );
    }

    public static LinkedHashSet<String> orderedFastStringSetDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        LinkedHashSet<String> items = new LinkedHashSet<>( size );
        for ( int i = 0; i < size; i++ ) {
            items.add( in.readUTF() );
        }
        return items;
    }

}
