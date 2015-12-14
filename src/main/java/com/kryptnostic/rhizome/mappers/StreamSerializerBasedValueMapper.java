package com.kryptnostic.rhizome.mappers;

import java.io.IOException;

import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import com.hazelcast.nio.BufferObjectDataInput;
import com.hazelcast.nio.BufferObjectDataOutput;
import com.hazelcast.nio.serialization.SerializationService;
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
        try ( BufferObjectDataOutput objOs = serializationService.createObjectDataOutput( 1 ) ) {
            serializer.write( objOs, value );
            return objOs.toByteArray();
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
    }

    @Override
    public T fromBytes( byte[] data ) throws MappingException {
        try ( BufferObjectDataInput in = serializationService.createObjectDataInput( data ) ) {
            return serializer.read( in );
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
    }

    @Override
    public Class<T> getClazz() {
        return serializer.getClazz();
    }

}
