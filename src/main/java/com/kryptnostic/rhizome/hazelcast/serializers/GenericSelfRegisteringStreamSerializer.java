package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;

public interface GenericSelfRegisteringStreamSerializer<T> extends SelfRegisteringStreamSerializer<T> {

    void serialize( ObjectOutput out, T object ) throws IOException;

    T deserialize( ObjectInput in ) throws IOException;
}
