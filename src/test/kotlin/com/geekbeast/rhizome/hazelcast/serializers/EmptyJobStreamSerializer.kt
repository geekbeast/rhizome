package com.geekbeast.rhizome.hazelcast.serializers

import com.geekbeast.mappers.mappers.ObjectMappers
import com.fasterxml.jackson.module.kotlin.readValue
import com.geekbeast.rhizome.jobs.DistributableJob
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer
import org.springframework.stereotype.Component

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
@Component
class EmptyJobStreamSerializer : SelfRegisteringStreamSerializer<DistributableJob<*>> {
    private val mapper = ObjectMappers.getSmileMapper()

    override fun getTypeId(): Int = TestStreamSerializersTypeIds.EMPTY_JOB.ordinal

    override fun write(out: ObjectDataOutput, `object`: DistributableJob<*>) {
        out.writeByteArray(mapper.writeValueAsBytes(`object`))
    }

    override fun read(`in`: ObjectDataInput): DistributableJob<*> = mapper.readValue(`in`.readByteArray()!!)
    override fun getClazz(): Class<out DistributableJob<*>> = DistributableJob::class.java
}