package com.kryptnostic.rhizome.hazelcast.serializers;

import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractStreamSerializerTest<T extends StreamSerializer<D>, D> {

    private final transient T serializer;
    private final transient T deserializer;

    public AbstractStreamSerializerTest() {
        serializer = createSerializer();
        deserializer = createDeserializer();
    }

    protected abstract T createSerializer();

    /**
     * Override this class when deserializer is expected to deal with encrypted data.
     */
    protected T createDeserializer() {
        return serializer;
    }

    protected abstract D createInput();

    @Test
    public void testSerializeDeserialize() throws SecurityException, IOException {

        D inputObject = createInput();
        InternalSerializationService ss = ( new DefaultSerializationServiceBuilder() ).build();

        ObjectDataOutput dataOut = ss.createObjectDataOutput( 1 );

        serializer.write( dataOut, inputObject );

        byte[] inputData = dataOut.toByteArray();

        ObjectDataInput dataIn = ss.createObjectDataInput( inputData );

        D outputObject = deserializer.read( dataIn );
        if ( inputObject instanceof Object[] || outputObject instanceof Object[] ) {
            Assert.assertArrayEquals( (Object[]) inputObject, (Object[]) outputObject );
        } else {
            testOutput( inputObject, outputObject );
        }
    }

    public void testOutput( @NotNull  D inputObject, @NotNull  D outputObject ) {
        Assert.assertEquals( inputObject, outputObject );
    }

}
