package com.geekbeast.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.UUID;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

/**
 * Abstract class for efficiently serializing two UUIDs as long in Hazelcast. Like other stream serializer provided it
 * is abstract so that users can define their serializer type ids.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public final class UUIDStreamSerializerUtils {
    private UUIDStreamSerializerUtils() {}

    public static void serialize( ObjectDataOutput out, UUID object ) throws IOException {
        out.writeLong( object.getLeastSignificantBits() );
        out.writeLong( object.getMostSignificantBits() );
    }

    public static UUID deserialize( ObjectDataInput in ) throws IOException {
        long lsb = in.readLong();
        long msb = in.readLong();
        return new UUID( msb, lsb );
    }
}
