package com.kryptnostic.rhizome.mappers;

import java.io.IOException;

import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.BufferObjectDataInput;
import com.hazelcast.nio.BufferObjectDataOutput;
import com.kryptnostic.rhizome.mapstores.MappingException;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

public class StreamSerializerBasedValueMapper<T> implements SelfRegisteringValueMapper<T> {

    private final SelfRegisteringStreamSerializer<T> serializer;
    private final InternalSerializationService                      serializationService;

    public StreamSerializerBasedValueMapper( SelfRegisteringStreamSerializer<T> serializer, InternalSerializationService serializationService ) {
        this.serializer = serializer;
        this.serializationService = serializationService;
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
    public Class<? extends T> getClazz() {
        return serializer.getClazz();
    }

    @Override
    public byte[] toBytes( T value, int bufferSize ) throws MappingException {
        try ( BufferObjectDataOutput objOs = serializationService.createObjectDataOutput( bufferSize ) ) {
            serializer.write( objOs, value );
            return objOs.toByteArray();
        } catch ( IOException e ) {
            throw new MappingException( e );
        }
    }

}
