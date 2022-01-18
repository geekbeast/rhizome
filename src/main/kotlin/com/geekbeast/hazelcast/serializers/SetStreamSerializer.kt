/*
 * Copyright (C) 2019. OpenLattice, Inc.
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
package com.geekbeast.hazelcast.serializers

import com.hazelcast.nio.ObjectDataInput
import java.io.IOException

abstract class SetStreamSerializer<T : MutableSet<E>, E> protected constructor(clazz: Class<T>)
    : CollectionStreamSerializer<T, E>(clazz) {

    @Throws(IOException::class)
    override fun read(input: ObjectDataInput): T {
        val size = input.readInt()
        val obj = newInstanceWithExpectedSize(size)
        for (i in 0 until size) {
            obj.add(readSingleElement(input))
        }
        return obj
    }
}