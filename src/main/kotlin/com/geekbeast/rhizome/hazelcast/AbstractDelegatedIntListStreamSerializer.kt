package com.geekbeast.rhizome.hazelcast

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.openlattice.hazelcast.serializers.TestableSelfRegisteringStreamSerializer

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
abstract class AbstractDelegatedIntListStreamSerializer : TestableSelfRegisteringStreamSerializer<DelegatedIntList> {
    override fun getClazz(): Class<out DelegatedIntList> {
        return DelegatedIntList::class.java
    }

    override fun write(out: ObjectDataOutput, obj: DelegatedIntList) {
        out.writeIntArray(obj.toIntArray())
    }

    override fun read(input: ObjectDataInput): DelegatedIntList {
        return DelegatedIntList(input.readIntArray().toList())
    }
}