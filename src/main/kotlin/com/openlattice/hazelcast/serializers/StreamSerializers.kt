package com.openlattice.hazelcast.serializers

import com.google.common.collect.Maps
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import java.util.*

class StreamSerializers {
    companion object {
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
            val keysMost    = `in`.readLongArray()
            val keysLeast   = `in`.readLongArray()

            val valsMost    = `in`.readLongArray()
            val valsLeast   = `in`.readLongArray()

            for ( i in 0 until keysMost.size ){
                map[ UUID( keysMost[ i ], keysLeast[ i ])] = UUID( valsMost[ i ], valsLeast[ i ])
            }

            return map
        }
    }
}
