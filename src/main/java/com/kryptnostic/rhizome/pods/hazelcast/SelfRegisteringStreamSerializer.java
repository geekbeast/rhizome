package com.kryptnostic.rhizome.pods.hazelcast;

import com.codahale.metrics.annotation.Timed;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;

public interface SelfRegisteringStreamSerializer<T> extends StreamSerializer<T> {
    Class<? extends T> getClazz();

    @Timed
    @Override void write( ObjectDataOutput out, T object ) throws IOException;

    @Timed
    @Override T read( ObjectDataInput in ) throws IOException;

    @Override default void destroy() {
    }
}
