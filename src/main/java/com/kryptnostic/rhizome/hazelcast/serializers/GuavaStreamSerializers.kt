/*
 * Copyright (C) 2018. OpenLattice, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 *
 */

package com.kryptnostic.rhizome.hazelcast.serializers

import com.google.common.collect.HashMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import java.util.*

/**
 *
 * Utility function for performing stream serializations of of set multimaps of a uuid to a set of string.
 */
fun serializeSetMultimap(out: ObjectDataOutput, mm: SetMultimap<UUID, String>) {
    out.writeInt(mm.entries().size)
    Multimaps.asMap(mm).forEach {
        val tags = it.value.toTypedArray()
        AbstractUUIDStreamSerializer.serialize(out, it.key)
        out.writeUTFArray(tags)
    }
}

/**
 * * Utility function for performing stream deserializations of of set multimaps of a uuid to a set of string.
 */
fun deserializeSetMultimap(input: ObjectDataInput): SetMultimap<UUID, String>? {
    val size = input.readInt()
    val mm = HashMultimap.create<UUID, String>()
    for (i in 0 until size) {
        val id = AbstractUUIDStreamSerializer.deserialize(input)
        mm.putAll(id, input.readUTFArray().asIterable())
    }
    return mm
}

/**
 * * Utility function for performing stream deserializations of of set multimaps of a uuid to a set of string.
 */
fun deserializeLinkedHashMultimap(input: ObjectDataInput): LinkedHashMultimap<UUID, String>? {
    val size = input.readInt()
    val mm = LinkedHashMultimap.create<UUID, String>()
    for (i in 0 until size) {
        val id = AbstractUUIDStreamSerializer.deserialize(input)
        mm.putAll(id, input.readUTFArray().asIterable())
    }
    return mm
}