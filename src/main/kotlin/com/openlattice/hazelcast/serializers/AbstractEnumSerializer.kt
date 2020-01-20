package com.openlattice.hazelcast.serializers

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import org.apache.commons.lang3.RandomUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class AbstractEnumSerializer<T: Enum<T>> : TestableSelfRegisteringStreamSerializer<T> {

    protected val enumArray = enumCache.getOrPut( clazz ) { ->
        this.clazz.enumConstants as Array<Enum<*>>
    } as Array<T>

    companion object {
        private var enumCache: ConcurrentMap<Class<*>, Array<Enum<*>>> = ConcurrentHashMap()

        @JvmStatic
        fun serialize(out: ObjectDataOutput, `object`: Enum<*>) {
            out.writeInt(`object`.ordinal)
        }

        @JvmStatic
        fun <K: Enum<K>> deserialize(targetClass: Class<out K>, `in`: ObjectDataInput): K {
            val ord = `in`.readInt()
            return (enumCache.computeIfAbsent( targetClass ) { key ->
                key.enumConstants as Array<Enum<*>>
            })[ord] as K
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
