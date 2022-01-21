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
import com.hazelcast.nio.ObjectDataOutput
import java.io.IOException

abstract class CollectionStreamSerializer<T : Collection<E>, E> protected constructor(private val clazz: Class<T>)
    : TestableSelfRegisteringStreamSerializer<T> {

    @Throws(IOException::class)
    override fun write(output: ObjectDataOutput, obj: T) {
        output.writeInt(obj.size)
        for (element in obj) {
            writeSingleElement(output, element)
        }
    }

    @Throws(IOException::class)
    protected abstract fun writeSingleElement(output: ObjectDataOutput, element: E)

    @Throws(IOException::class)
    protected abstract fun readSingleElement(input: ObjectDataInput): E

    protected abstract fun newInstanceWithExpectedSize(size: Int): T

    override fun destroy() {}

    override fun getClazz(): Class<T> {
        return clazz
    }
}