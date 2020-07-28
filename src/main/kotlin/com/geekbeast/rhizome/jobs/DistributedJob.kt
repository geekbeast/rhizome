/*
 * Copyright (C) 2020. OpenLattice, Inc.
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

package com.geekbeast.rhizome.jobs

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.HazelcastInstanceAware
import java.util.*
import java.util.concurrent.Callable

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
abstract class DistributedJob<T>(val id: UUID,val state: DistributedJobState) : Callable<T>, HazelcastInstanceAware {
    private lateinit var hazelcastInstance: HazelcastInstance

    override fun setHazelcastInstance(hazelcastInstance: HazelcastInstance) {
        this.hazelcastInstance= hazelcastInstance
    }

    override fun call(): T {


    }

    fun saveProgress() {
        val jobs = hazelcastInstance.getMap<UUID, DistributedJob<*>>()
        jobs.set(id, this)
    }
}