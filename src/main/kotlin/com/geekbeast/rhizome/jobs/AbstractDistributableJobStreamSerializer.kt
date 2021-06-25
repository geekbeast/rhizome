package com.geekbeast.rhizome.jobs

import com.dataloom.mappers.ObjectMappers
import com.fasterxml.jackson.module.kotlin.readValue
import com.geekbeast.rhizome.jobs.DistributableJob
import com.geekbeast.rhizome.jobs.EmptyJob
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer
import org.springframework.stereotype.Component

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
abstract class AbstractDistributableJobStreamSerializer : SelfRegisteringStreamSerializer<DistributableJob<*>> {
    private val mapper = ObjectMappers.getSmileMapper()

    override fun write(out: ObjectDataOutput, `object`: DistributableJob<*>) {
        out.writeByteArray(mapper.writeValueAsBytes(`object`))
    }

    override fun read(`in`: ObjectDataInput): DistributableJob<*> = mapper.readValue(`in`.readByteArray()!!)
    override fun getClazz(): Class<out DistributableJob<*>> = DistributableJob::class.java
}