package com.openlattice.hazelcast.serializers

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput

abstract class AbstractEnumSerializer<T: Enum<T>> : TestableSelfRegisteringStreamSerializer<T> {

    companion object {
        private val enumCache: LoadingCache<Class<out Enum<*>>, Array<out Enum<*>>> = CacheBuilder.newBuilder().build(
                CacheLoader.from { key ->
                    key!!.enumConstants
                }
        )

        @JvmStatic
        fun serialize(out: ObjectDataOutput, `object`: Enum<*>) {
            out.writeInt(`object`.ordinal)
        }

        @JvmStatic
        fun <K: Enum<K>> deserialize(targetClass: Class<out K>, `in`: ObjectDataInput): K {
            val ord = `in`.readInt()
            return enumCache.get(targetClass)[ord] as K
        }
    }

    override fun write(out: ObjectDataOutput, `object`: T) {
        return serialize(out, `object`)
    }

    override fun read(`in`: ObjectDataInput): T {
        return deserialize(clazz, `in`)
    }
}
