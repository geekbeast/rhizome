package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;

import com.datastax.driver.core.PagingState;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.hazelcast.objects.PagedResponse;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

public class PagedResponseStreamSerializer<T> implements SelfRegisteringStreamSerializer<PagedResponse> {

    private int                                         serializerTypeId;
    private IoPerformingBiConsumer<ObjectDataOutput, T> serializer;
    private IoPerformingFunction<ObjectDataInput, T>    deSerializer;

    public PagedResponseStreamSerializer(
            Class<T> containedClazz,
            int serializerTypeId,
            IoPerformingBiConsumer<ObjectDataOutput, T> serializer,
            IoPerformingFunction<ObjectDataInput, T> deserializer ) {
        this.serializerTypeId = serializerTypeId;
        this.serializer = serializer;
        this.deSerializer = deserializer;
    }

    @Override
    public void write( ObjectDataOutput out, PagedResponse object ) throws IOException {
        PagedResponse<T> resp = object;
        serializer.accept( out, resp.getResults() );
        PagingState pagingState = object.getPagingState();
        boolean exists = pagingState != null;
        out.writeBoolean( exists );
        if ( exists ) {
            out.writeByteArray( pagingState.toBytes() );
        }
    }

    @Override
    public PagedResponse<T> read( ObjectDataInput in ) throws IOException {
        T apply = deSerializer.apply( in );
        PagingState pState = null;
        if ( in.readBoolean() ) {
            pState = PagingState.fromBytes( in.readByteArray() );
        }
        return new PagedResponse<T>( apply, pState );
    }

    @Override
    public int getTypeId() {
        return serializerTypeId;
    }

    @Override
    public void destroy() { /* */ }

    @Override
    public Class<PagedResponse> getClazz() {
        return PagedResponse.class;
    }

}
