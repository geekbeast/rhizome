package com.kryptnostic.rhizome.mappers.values;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.kryptnostic.rhizome.mappers.ValueMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class UUIDValueMapper implements ValueMapper<UUID> {

    @Override
    public byte[] toBytes( UUID value ) throws MappingException {
        ByteBuffer out = ByteBuffer.allocate( 2 * Long.BYTES );
        out.putLong( value.getLeastSignificantBits() );
        out.putLong( value.getMostSignificantBits() );
        return out.array();
    }

    @Override
    public UUID fromBytes( byte[] data ) throws MappingException {
        ByteBuffer in = ByteBuffer.wrap( data );
        long lsb = in.getLong();
        long msb = in.getLong();
        return new UUID( msb, lsb );
    }

}
