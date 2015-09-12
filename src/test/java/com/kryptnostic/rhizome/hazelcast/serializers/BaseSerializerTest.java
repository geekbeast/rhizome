package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.hazelcast.nio.serialization.DefaultSerializationServiceBuilder;
import com.hazelcast.nio.serialization.ObjectDataInputStream;
import com.hazelcast.nio.serialization.ObjectDataOutputStream;
import com.hazelcast.nio.serialization.SerializationService;
import com.hazelcast.nio.serialization.StreamSerializer;

public abstract class BaseSerializerTest<T extends StreamSerializer<D>, D> {

    private final T serializer;
    private final T deserializer;

    public BaseSerializerTest() {
        serializer = createSerializer();
        deserializer = createDeserializer();
    }

    protected abstract T createSerializer();
    
    /**
     * Override this class when deserializer is expected to deal with encrypted data.
     * @return
     */
    protected T createDeserializer() {
        return serializer;
    }

    protected abstract D createInput();

    @Test
    public void testSerializeDeserialize() throws NoSuchMethodException, SecurityException, IOException {
        D inputObject = createInput();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SerializationService ss = ( new DefaultSerializationServiceBuilder() ).build();
        ObjectDataOutputStream dataOut = ss.createObjectDataOutputStream( out );

        serializer.write( dataOut, inputObject );

        byte[] inputData = out.toByteArray();

        InputStream in = new ByteArrayInputStream( inputData );
        ObjectDataInputStream dataIn = ss.createObjectDataInputStream( in );
        
        D outputObject = (D) deserializer.read( dataIn );

        Assert.assertEquals( inputObject, outputObject );
    }

}
