package com.geekbeast.rhizome.hazelcast

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
abstract class AbstractDelegatedIntListStreamSerializer : SelfRegisteringStreamSerializer<DelegatedIntList> {
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