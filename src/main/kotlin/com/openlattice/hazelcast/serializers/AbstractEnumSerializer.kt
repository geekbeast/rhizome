package com.openlattice.hazelcast.serializers

import com.google.common.collect.Maps
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import org.apache.commons.lang3.RandomUtils
import java.util.concurrent.ConcurrentMap

abstract class AbstractEnumSerializer<T: Enum<T>> : TestableSelfRegisteringStreamSerializer<T> {

    protected val enumArray = enumCache.getOrPut(clazz) { this.clazz.enumConstants as Array<Enum<*>> } as Array<T>

    companion object {
        private val enumCache: ConcurrentMap<Class<*>, Array<Enum<*>>> = Maps.newConcurrentMap()

        @JvmStatic
        fun serialize(out: ObjectDataOutput, `object`: Enum<*>) {
            out.writeInt(`object`.ordinal)
        }

        @JvmStatic
        fun <K: Enum<K>> deserialize(targetClass: Class<out K>, `in`: ObjectDataInput): K {
            val ord = `in`.readInt()
            return enumCache.getValue(targetClass)[ord] as K
        }
    }

    override fun write(out: ObjectDataOutput, `object`: T) {
        return serialize(out, `object`)
    }

    override fun read(`in`: ObjectDataInput): T {
        return enumArray[`in`.readInt()]
    }

    override fun generateTestValue(): T {
        return enumArray[RandomUtils.nextInt(0, enumArray.size)]
    }
}