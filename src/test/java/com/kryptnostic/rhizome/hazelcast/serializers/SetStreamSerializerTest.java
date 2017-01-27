package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Random;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class SetStreamSerializerTest extends AbstractStreamSerializerTest<StreamSerializer<TestSet>, TestSet> {

    @Override
    protected StreamSerializer<TestSet> createSerializer() {
        return new SetStreamSerializer<TestSet, Long>( TestSet.class ) {

            @Override
            public int getTypeId() {
                return 1;
            }

            @Override
            protected TestSet newInstanceWithExpectedSize( int size ) {
                return new TestSet();
            }

            @Override
            protected Long readSingleElement( ObjectDataInput in ) throws IOException {
                return in.readLong();
            }

            @Override
            protected void writeSingleElement( ObjectDataOutput out, Long element ) throws IOException {
                out.writeLong( element );
            }

        };
    }

    @Override
    protected TestSet createInput() {
        TestSet s = new TestSet();
        Random r = new Random();
        for ( int i = 0; i < 10; ++i ) {
            s.add( r.nextLong() );
        }
        return s;
    }
}
