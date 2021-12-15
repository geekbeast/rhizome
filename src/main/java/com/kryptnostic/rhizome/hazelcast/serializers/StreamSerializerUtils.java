package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public final class StreamSerializerUtils {
    private StreamSerializerUtils() {}

    public static void writeStringList( ObjectDataOutput out, List<String> l ) throws IOException {
        out.writeInt( l.size() );
        for ( String elem : l ) {
            out.writeUTF( elem );
        }
    }

    public static List<String> readStringArrayList( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        String[] elems = new String[ size ];
        for ( int i = 0; i < size; ++i ) {
            elems[ i ] = in.readString();
        }
        return Arrays.asList( elems );
    }

}
