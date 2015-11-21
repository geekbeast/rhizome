package com.kryptnostic.rhizome.mappers;

import java.io.IOException;

import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class StreamSerializerBasedValueMapper<T> implements SelfRegisteringValueMapper<T> {

    private final SelfRegisteringStreamSerializer<T> serializer;
    private final SerializationService                      serializationService;

    public StreamSerializerBasedValueMapper( SelfRegisteringStreamSerializer<T> serializer, SerializationService serializationService ) {
        this.serializer = serializer;
        this.serializationService = serializationService;
    }

    @Override
    public byte[] toBytes( T value ) throws MappingException {
        ObjectDataOutput objOs = serializationService.createObjectDataOutput();
        try {
            serializer.write( objOs, value );
            return objOs.toByteArray();
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
    }

    @Override
    public T fromBytes( byte[] data ) throws MappingException {
        ObjectDataInput in = serializationService.createObjectDataInput( data );
        T deserialize;
        try {
            deserialize = serializer.read( in );
            return deserialize;
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
    }

    @Override
    public Class<T> getClazz() {
        return serializer.getClazz();
    }

}
