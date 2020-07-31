package com.geekbeast.rhizome.hazelcast.serializers

import com.dataloom.mappers.ObjectMappers
import com.fasterxml.jackson.module.kotlin.readValue
import com.geekbeast.rhizome.jobs.EmptyJob
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer
import org.springframework.stereotype.Component

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Component
class EmptyJobStreamSerializer : SelfRegisteringStreamSerializer<EmptyJob> {
    private val mapper = ObjectMappers.getSmileMapper()

    override fun getTypeId(): Int = TestStreamSerializersTypeIds.EMPTY_JOB.ordinal

    override fun write(out: ObjectDataOutput, `object`: EmptyJob) {
        out.writeByteArray(mapper.writeValueAsBytes(`object`))
    }

    override fun read(`in`: ObjectDataInput): EmptyJob = mapper.readValue(`in`.readByteArray())
    override fun getClazz(): Class<out EmptyJob> = EmptyJob::class.java
}