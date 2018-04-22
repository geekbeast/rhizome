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

package com.openlattice.jdbc

import com.fasterxml.jackson.annotation.JsonCreator
import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import com.zaxxer.hikari.HikariDataSource

/**
 *
 */
class HikariDataSources(val datasources: Map<String, HikariDataSource>, val allocator: HashFunction) {
    val rangeSize = Long.MAX_VALUE / datasources.size

    @JsonCreator
    constructor(dsMap: Map<String, HikariDataSource>) : this(dsMap, Hashing.murmur3_128() ) {

    }
//
//    fun getByName( name : String  ): HikariDataSource? {
//        return datasources[name]
//    }
//
//    fun getByTag( tag: String ) : HikariDataSource {
////        val allocator.hashString( tag ).asLong()/rangeSize
//        return
//    }
}