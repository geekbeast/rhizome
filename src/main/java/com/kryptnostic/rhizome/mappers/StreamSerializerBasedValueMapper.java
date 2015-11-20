package com.kryptnostic.rhizome.mappers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.kryptnostic.rhizome.hazelcast.serializers.GenericSelfRegisteringStreamSerializer;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class StreamSerializerBasedValueMapper<T> implements SelfRegisteringValueMapper<T> {

    private final GenericSelfRegisteringStreamSerializer<T> serializer;

    public StreamSerializerBasedValueMapper( GenericSelfRegisteringStreamSerializer<T> serializer ) {
        this.serializer = serializer;
    }

    @Override
    public byte[] toBytes( T value ) throws MappingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutput objOs;
        try {
            objOs = new ObjectOutputStream( os );
            serializer.serialize( objOs, value );
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
        return os.toByteArray();
    }

    @Override
    public T fromBytes( byte[] data ) throws MappingException {
        ByteArrayInputStream is = new ByteArrayInputStream( data );
        ObjectInput in;
        T deserialize;
        try {
            in = new ObjectInputStream( is );
            deserialize = serializer.deserialize( in );
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
        return deserialize;
    }

    @Override
    public Class<T> getClazz() {
        return serializer.getClazz();
    }

}
