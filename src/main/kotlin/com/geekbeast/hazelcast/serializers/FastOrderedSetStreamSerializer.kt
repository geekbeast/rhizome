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

import com.geekbeast.rhizome.KotlinDelegatedStringSet
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import java.io.IOException

 abstract class AbstractKotlinDelegatedStringSet : TestableSelfRegisteringStreamSerializer<KotlinDelegatedStringSet> {
     override fun generateTestValue(): KotlinDelegatedStringSet {
         return KotlinDelegatedStringSet(setOf("a","b","c"))
     }


     override fun write(out: ObjectDataOutput, `object`: KotlinDelegatedStringSet) {
         out.writeStringArray(`object`.toTypedArray())
     }

     @Throws(IOException::class)
    override fun read(input: ObjectDataInput): KotlinDelegatedStringSet {
        return KotlinDelegatedStringSet(input.readStringArray()!!.toSet() )
    }

     override fun getClazz(): Class<out KotlinDelegatedStringSet> {
         return KotlinDelegatedStringSet::class.java
     }
 }