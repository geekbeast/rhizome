package com.openlattice.hazelcast.serializers

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import java.util.*

class KotlinOptionalStreamSerializers {
    companion object {

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

