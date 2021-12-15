package com.openlattice.hazelcast.serializers

import com.google.common.collect.Maps
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import java.util.*

class StreamSerializers {
    companion object {

        @JvmStatic
        fun <T> serializeSet(out: ObjectDataOutput, elements: Set<T>, serializeFunc: (T) -> Unit ) {
            out.writeInt(elements.size)
            for (elem in elements) {
                serializeFunc(elem)
            }
        }

        @JvmStatic
        fun <T> deserializeSet(`in`: ObjectDataInput, set: MutableSet<T> = mutableSetOf(), deserializeFunc: () -> T ): MutableSet<T> {
            val size = `in`.readInt()
            repeat(size) {
                set.add(deserializeFunc())
            }
            return set
        }

        @JvmStatic
        fun serializeIntList(out: ObjectDataOutput, `object`: Collection<Int> ) {
            out.writeIntArray(`object`.toIntArray())
        }

        @JvmStatic
        fun deserializeIntList(`in`: ObjectDataInput, collection: MutableCollection<Int> = mutableListOf() ): MutableCollection<Int> {
            val data = `in`.readIntArray()!!
            for (i in data) {
                collection.add(i)
            }
            return collection
        }

        @JvmStatic
        fun serializeUUIDUUIDMap( out: ObjectDataOutput, map: Map<UUID, UUID> ) {
            val keysMost    = LongArray( map.size )
            val keysLeast   = LongArray( map.size )
            val valsMost    = LongArray( map.size )
            val valsLeast   = LongArray( map.size )

            for ( ( i, entry ) in map.entries.withIndex() ) {
                keysMost[ i ]   = entry.key.mostSignificantBits
                keysLeast[ i ]  = entry.key.leastSignificantBits
                valsMost[ i ]   = entry.value.mostSignificantBits
                valsLeast[ i ]  = entry.value.leastSignificantBits
            }

            out.writeLongArray( keysMost )  // keys most significant bits
            out.writeLongArray( keysLeast ) // keys least significant bits

            out.writeLongArray( valsMost )  // vals most significant bits
            out.writeLongArray( valsLeast ) // vals least significant bits
        }

        @JvmStatic
        fun deserializeUUIDUUIDMap( `in`: ObjectDataInput, map: MutableMap<UUID, UUID> = Maps.newLinkedHashMap() ): Map<UUID, UUID> {
            val keysMost    = `in`.readLongArray()!!
            val keysLeast   = `in`.readLongArray()!!

            val valsMost    = `in`.readLongArray()!!
            val valsLeast   = `in`.readLongArray()!!

            for ( i in keysMost.indices){
                map[ UUID( keysMost[ i ], keysLeast[ i ])] = UUID( valsMost[ i ], valsLeast[ i ])
            }

            return map
        }

        @JvmStatic
        fun serializeStringStringMap(out: ObjectDataOutput, map: Map<String, String> ) {
            val pairs = arrayOfNulls<String>(map.size * 2)

            for ( ( i, entry ) in map.entries.withIndex() ) {
                pairs[ i ]   = entry.key
                pairs[ i + map.size ]   = entry.value
            }

            out.writeInt( map.size )
            out.writeUTFArray( pairs )
        }

        @JvmStatic
        fun deserializeStringStringMap( `in`: ObjectDataInput): Map<String, String> {
            val size = `in`.readInt()
            val map = Maps.newLinkedHashMapWithExpectedSize<String, String>( size )

            val pairs = `in`.readStringArray()!!

            for (index in 0 until size) {
                map.put( pairs[index], pairs[index + size] )
            }

            return map
        }

        @JvmStatic
        fun <K, V> serializeMap(out: ObjectDataOutput, `object`: Map<K, V>, keyWritingFun: (K) -> Unit, valWritingFun: (V) -> Unit ) {
            out.writeInt(`object`.size)
            for ( entry in `object`.entries ) {
                keyWritingFun(entry.key)
                valWritingFun(entry.value)
            }
        }

        @JvmStatic
        fun <K, V> deserializeMap( `in`: ObjectDataInput, keyMappingFun: (ObjectDataInput) -> K, valMappingFun: (ObjectDataInput) -> V ): Map<K, V> {
            val size = `in`.readInt()
            val map = mutableMapOf<K, V>()
            repeat(size) {
                val key = keyMappingFun(`in`)
                val value = valMappingFun(`in`)
                map[key] = value
            }
            return map
        }

        @JvmStatic
        fun <K, K2, V> serializeMapMap( out: ObjectDataOutput,
                                        `object`: Map<K, Map<K2, V>>,
                                        keyWritingFun: (K) -> Unit,
                                        subKeyWritingFun: (K2) -> Unit,
                                        valWritingFun: (V) -> Unit ) {
            out.writeInt(`object`.size)
            for ( entry in `object`.entries ) {
                keyWritingFun(entry.key)
                out.writeInt(entry.value.size)
                for ( subEntry in entry.value ) {
                    subKeyWritingFun(subEntry.key)
                    valWritingFun(subEntry.value)
                }
            }
        }

        @JvmStatic
        fun <K, K2, V> deserializeMapMap(`in`: ObjectDataInput,
                                         map: MutableMap<K, MutableMap<K2, V>> = mutableMapOf(),
                                         keyMappingFun: (ObjectDataInput) -> K,
                                         subKeyMappingFun: (ObjectDataInput) -> K2,
                                         valMappingFun: (ObjectDataInput) -> V
        ): MutableMap<K, MutableMap<K2, V>> {
            val size = `in`.readInt()
            repeat(size) {
                val k1 = keyMappingFun(`in`)
                val innerSize = `in`.readInt()
                val innerMap = mutableMapOf<K2, V>()
                repeat(innerSize) {
                    val subKey = subKeyMappingFun(`in`)
                    val value = valMappingFun(`in`)
                    innerMap[subKey] = value
                }
                map[k1] = innerMap
            }
            return map
        }

        @JvmStatic
        inline fun <T> serializeMaybeValue(out: ObjectDataOutput, value: T?, write: (ObjectDataOutput) -> Unit ) {
            if ( value == null ){
                out.writeBoolean( false )
                return
            }
            out.writeBoolean( true )
            write( out )
        }

        @JvmStatic
        inline fun <T> deserializeMaybeValue(`in`: ObjectDataInput, read: (ObjectDataInput) -> T ): T? {
            val maybePresent = `in`.readBoolean()
            if ( !maybePresent ){
                return null
            }
            return read( `in` )
        }

        @JvmStatic
        inline fun <T> serializeOptionalValue(out: ObjectDataOutput, value: Optional<T>, write: (ObjectDataOutput) -> Unit ) {
            if ( !value.isPresent ){
                out.writeBoolean( false )
                return
            }
            out.writeBoolean( true )
            write( out )
        }

        @JvmStatic
        inline fun <T> deserializeOptional(`in`: ObjectDataInput, read: (ObjectDataInput) -> T ): Optional<T> {
            val maybePresent = `in`.readBoolean()
            if ( !maybePresent ){
                return Optional.empty()
            }
            return Optional.of( read( `in` ) )
        }

    }
}
