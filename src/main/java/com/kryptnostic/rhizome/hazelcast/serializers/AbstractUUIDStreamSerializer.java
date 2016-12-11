package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.UUID;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

/**
 * Abstract class for efficiently serializing two UUIDs as long in Hazelcast. Like other stream serializer provided it
 * is abstract so that users can define their serializer type ids.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public abstract class AbstractUUIDStreamSerializer implements SelfRegisteringStreamSerializer<UUID> {

    @Override
    public void destroy() {

    }

    @Override
    public void write( ObjectDataOutput out, UUID object ) throws IOException {
        serialize( out, object );
    }

    @Override
    public UUID read( ObjectDataInput in ) throws IOException {
        return deserialize( in );
    }

    public static void serialize( ObjectDataOutput out, UUID object ) throws IOException {
        out.writeLong( object.getLeastSignificantBits() );
        out.writeLong( object.getMostSignificantBits() );
    }

    public static UUID deserialize( ObjectDataInput in ) throws IOException {
        long lsb = in.readLong();
        long msb = in.readLong();
        return new UUID( msb, lsb );
    }

    @Override
    public Class<UUID> getClazz() {
        return UUID.class;
    }
}
