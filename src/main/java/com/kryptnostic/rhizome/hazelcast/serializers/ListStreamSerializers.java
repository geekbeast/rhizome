package com.kryptnostic.rhizome.hazelcast.serializers;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import com.openlattice.rhizome.hazelcast.DelegatedUUIDList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class ListStreamSerializers {
    public static List<UUID> fastUUIDListDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        return processEntries( size, in );
    }

    public static UUID[] fastUUIDArrayDeserialize( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        return processEntriesToArray( size, in );
    }

    public static void fastUUIDListSerialize( ObjectDataOutput out, List<UUID> target ) throws IOException {
        long[] least = new long[ target.size() ];
        long[] most = new long[ target.size() ];
        int i = 0;
        for ( UUID uuid : target ) {
            least[ i ] = uuid.getLeastSignificantBits();
            most[ i ] = uuid.getMostSignificantBits();
            i++;
        }
        out.writeInt( i );
        out.writeLongArray( least );
        out.writeLongArray( most );
    }

    private static List<UUID> processEntries( int size, ObjectDataInput in ) throws IOException {
        return Arrays.asList( processEntriesToArray(size, in) );
    }

    private static UUID[] processEntriesToArray( int size, ObjectDataInput in ) throws IOException {
        long[] least = in.readLongArray();
        long[] most = in.readLongArray();
        UUID[] uuids = new UUID[ size ];
        for ( int i = 0; i < size; i++ ) {
            uuids[ i ] = new UUID( most[ i ], least[ i ] );
        }
        return uuids;
    }

    public abstract static class DelegatedUUIDListStreamSerializer
            implements SelfRegisteringStreamSerializer<DelegatedUUIDList> {

        @Override
        public Class<? extends DelegatedUUIDList> getClazz() {
            return DelegatedUUIDList.class;
        }

        @Override
        public void write( ObjectDataOutput out, DelegatedUUIDList object ) throws IOException {
            ListStreamSerializers.fastUUIDListSerialize( out, object );
        }

        @Override
        public DelegatedUUIDList read( ObjectDataInput in ) throws IOException {
            return DelegatedUUIDList.wrap( fastUUIDListDeserialize( in ) );
        }

        @Override
        public void destroy() {

        }
    }
}
