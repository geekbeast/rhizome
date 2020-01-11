package com.openlattice.rhizome.core.service

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

import com.google.common.util.concurrent.ListeningExecutorService
import com.hazelcast.core.HazelcastInstance
import com.openlattice.rhizome.hazelcast.ChunkedQueueSequence
import org.slf4j.Logger
import java.time.Instant
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

internal const val DEFAULT_BATCH_TIMEOUT_MILLIS = 120_000L

abstract class ContinuousRepeatingTaskService<T: Any>(
        private val executor: ListeningExecutorService,
        hazelcastInstance: HazelcastInstance,
        private val logger: Logger,
        queueName: String,
        lockingMapName: String,
        parallelism: Int,
        private val chunkSize: Int,
        private val batchTimeout: Long = DEFAULT_BATCH_TIMEOUT_MILLIS
) {
    private val locks = hazelcastInstance.getMap<T, Long>(lockingMapName)
    private val candidates = hazelcastInstance.getQueue<T>(queueName)
    private val limiter = Semaphore(parallelism)

    private val enqueuer = executor.submit {
        startupChecks()
        while (true) {
            try {
                sourceSet()
                    .filter {
                        val expiration = lockOrGetExpiration(it)
                        logger.debug(
                                "Considering candidate {} with expiration {} at {}",
                                it,
                                expiration,
                                Instant.now().toEpochMilli()
                        )
                        if (expiration != null && Instant.now().toEpochMilli() >= expiration) {
                            logger.info("Refreshing expiration for {}", it)
                            //Assume original lock holder died, probably somewhat unsafe
                            refreshExpiration(it)
                            true
                        } else expiration == null
                    }
                    .chunked(chunkSize)
                    .forEach { keys ->
                        candidates.addAll(keys)
                        logger.info(
                                "Queued entities needing processing {}.",
                                keys
                        )
                    }
            } catch (ex: Exception) {
                logger.info("Encountered error while enqueuing candidates for task.", ex)
            }
        }
    }

    abstract fun startupChecks()

    abstract fun operate(candidate: List<T>)

    abstract fun sourceSet() : Sequence<T>

    @Suppress("UNUSED")
    private val taskWorker = executor.submit {
        ChunkedQueueSequence<T>( candidates, 100 )
                .map { candidatesChunk ->
                    limiter.acquire()
                    executor.submit {
                        try {
                            logger.info("Operating on {}", candidatesChunk)
                            operate(candidatesChunk)
                        } catch (ex: Exception) {
                            logger.error("Unable to operate on $candidatesChunk. ", ex)
                        } finally {
                            locks.delete(candidatesChunk)
                            limiter.release()
                        }
                    }
                }.forEach { it.get() }
    }

    /**
     * @return Null if locked, expiration in millis otherwise.
     */
    fun lockOrGetExpiration(candidate: T): Long? {
        return locks.putIfAbsent(
                candidate,
                Instant.now().plusMillis(batchTimeout).toEpochMilli(),
                batchTimeout,
                TimeUnit.MILLISECONDS
        )
    }

    /**
     * @return Null if locked, expiration in millis otherwise.
     */
    fun refreshExpiration(candidate: T) {
        try {
            locks.lock(candidate)

            locks.putIfAbsent(
                    candidate,
                    Instant.now().plusMillis(batchTimeout).toEpochMilli(),
                    batchTimeout,
                    TimeUnit.MILLISECONDS
            )
        } finally {
            locks.unlock(candidate)
        }
    }
}

