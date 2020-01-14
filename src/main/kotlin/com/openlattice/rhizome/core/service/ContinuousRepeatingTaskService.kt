package com.openlattice.rhizome.core.service

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

import com.google.common.util.concurrent.ListeningExecutorService
import com.hazelcast.core.IMap
import com.hazelcast.core.IQueue
import org.slf4j.Logger
import java.time.Instant
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

internal const val DEFAULT_BATCH_TIMEOUT_MILLIS = 120_000L

abstract class ContinuousRepeatingTaskService<T: Any, K: Any>(
        private val executor: ListeningExecutorService,
        private val logger: Logger,
        private val candidateQueue: IQueue<T>,
        private val lockingMap: IMap<K, Long>,
        parallelism: Int,
        private val candidateLockFunc: (T) -> K,
        private val fillChunkSize: Int = 0,
        private val batchTimeout: Long = DEFAULT_BATCH_TIMEOUT_MILLIS
) {
    private val limiter = Semaphore(parallelism)

    @Suppress("UNUSED")
    private val enqueuer = if ( enqueuerEnabledCheck() ) {
        executor.submit {
            while (true) {
                try {
                    sourceSequence()
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
                            }.chunked(fillChunkSize)
                            .forEach { keys ->
                                candidateQueue.addAll(keys)
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
    } else {
        logger.info("Skipping enqueue task as it is not enabled.")
        null
    }

    @Suppress("UNUSED")
    private val worker = if ( workerEnabledCheck() ) {
        executor.submit {
            while ( true ) {
                try {
                    generateSequence { candidateQueue.take() }
                            .map { candidate ->
                                limiter.acquire()
                                executor.submit {
                                    try {
                                        logger.info("Operating on {}", candidate)
                                        operate(candidate)
                                    } catch (ex: Exception) {
                                        logger.error("Unable to operate on $candidate. ", ex)
                                    } finally {
                                        lockingMap.delete(candidate)
                                        limiter.release()
                                    }
                                }
                            }.forEach { it.get() }
                } catch ( ex: Exception) {
                    logger.info("Encountered error while operating on candidates.", ex )
                }
            }
        }
    } else {
        logger.info("Skipping worker task as it is not enabled.")
        null
    }

    abstract fun enqueuerEnabledCheck(): Boolean

    abstract fun workerEnabledCheck(): Boolean

    abstract fun operate(candidate: T)

    abstract fun sourceSequence() : Sequence<T>

    /**
     * @return Null if locked, expiration in millis otherwise.
     */
    fun lockOrGetExpiration(candidate: T): Long? {
        return lockingMap.putIfAbsent(
                candidateLockFunc(candidate),
                Instant.now().plusMillis(batchTimeout).toEpochMilli(),
                batchTimeout,
                TimeUnit.MILLISECONDS
        )
    }

    /**
     * Refreshes the candidate's timeout in the locking map
     */
    fun refreshExpiration(candidate: T) {
        try {
            lockingMap.lock(candidateLockFunc(candidate))

            lockOrGetExpiration(candidate)
        } finally {
            lockingMap.unlock(candidateLockFunc(candidate))
        }
    }
}
