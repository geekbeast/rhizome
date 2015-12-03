package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
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
    public void testSerializeDeserialize() throws SecurityException, IOException {
        D inputObject = createInput();

        SerializationService ss = ( new DefaultSerializationServiceBuilder() ).build();
        ObjectDataOutput dataOut = ss.createObjectDataOutput();

        serializer.write( dataOut, inputObject );

        byte[] inputData = dataOut.toByteArray();

        ObjectDataInput dataIn = ss.createObjectDataInput( inputData );

        D outputObject = deserializer.read( dataIn );

        Assert.assertEquals( inputObject, outputObject );
    }

}
