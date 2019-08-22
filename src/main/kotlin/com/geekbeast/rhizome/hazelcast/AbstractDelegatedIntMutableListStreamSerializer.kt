package com.geekbeast.rhizome.hazelcast

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
abstract class AbstractDelegatedIntMutableListStreamSerializer : SelfRegisteringStreamSerializer<DelegatedMutableIntList> {
    override fun getClazz(): Class<out DelegatedMutableIntList> {
        return DelegatedMutableIntList::class.java
    }

    override fun write(out: ObjectDataOutput, obj: DelegatedMutableIntList) {
        out.writeIntArray(obj.toIntArray())
    }

    override fun read(input: ObjectDataInput): DelegatedMutableIntList {
        return DelegatedMutableIntList(input.readIntArray().toMutableList())
    }
}