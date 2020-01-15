package com.openlattice.rhizome.service

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
import com.hazelcast.map.listener.EntryEvictedListener
import org.slf4j.Logger
import java.time.Instant
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

internal const val DEFAULT_BATCH_TIMEOUT_MILLIS = 120_000L

abstract class ContinuousRepeatingTaskService<T: Any, K: Any>(
        private val executor: ListeningExecutorService,
        private val logger: Logger,
        private val candidateQueue: IQueue<T>,
        private val statusMap: IMap<K, QueueState>,
        parallelism: Int,
        private val candidateLockFunc: (T) -> K,
        private val fillChunkSize: Int = 0,
        private val batchTimeout: Long = DEFAULT_BATCH_TIMEOUT_MILLIS
) {
    private val limiter = Semaphore(parallelism)

    init {
        statusMap.addEntryListener(EntryEvictedListener<K, QueueState> {
            logger.info("Key ${it.key} with expiration got evicted at ${Instant.now().toEpochMilli()}")
            statusMap.put( it.key, QueueState.NOT_QUEUED )
        }, true)
    }

    @Suppress("UNUSED")
    private val enqueuer = if ( enqueuerEnabledCheck() ) {
        executor.submit {
            while (true) {
                try {
                    sourceSequence()
                            .filter {
                                logger.debug("Queueing candidate {}", it)
                                filterEnqueued( it )
                            }
                            .chunked(fillChunkSize)
                            .forEach {
                                candidateQueue.addAll(it)
                            }
                } catch (ex: Exception) {
                    logger.info("Encountered error while enqueuing candidates for $javaClass task.", ex)
                }
            }
        }
    } else {
        logger.info("Skipping $javaClass enqueue task as it is not enabled.")
        null
    }

    @Suppress("UNUSED")
    private val worker = if ( workerEnabledCheck() ) {
        executor.submit {
            while ( true ) {
                try {
                    generateSequence { dequeue() }
                            .map { candidate ->
                                limiter.acquire()
                                executor.submit {
                                    try {
                                        logger.info("Operating on {}", candidate)
                                        operate(candidate)
                                    } catch (ex: Exception) {
                                        logger.error("Unable to operate on $candidate. ", ex)
                                    } finally {
                                        finishedProcessing( candidate )
                                        limiter.release()
                                    }
                                }
                            }.forEach { job ->
                                job.get( batchTimeout, TimeUnit.MILLISECONDS )
                            }
                } catch ( ex: Exception) {
                    logger.info("Encountered error while operating on candidates.", ex )
                }
            }
        }
    } else {
        logger.info("Skipping $javaClass worker task as it is not enabled.")
        null
    }

    abstract fun enqueuerEnabledCheck(): Boolean

    abstract fun workerEnabledCheck(): Boolean

    abstract fun operate(candidate: T)

    abstract fun sourceSequence() : Sequence<T>

    /**
     * @return Boolean indicating whether candidate was successfully queued
     */
    fun filterEnqueued( candidate: T ): Boolean {
        return statusMap.putIfAbsent(candidateLockFunc(candidate), QueueState.QUEUED) != null
    }

    /**
     * Take()s from candidateQueue continuously until an element with QueueState.QUEUED is returned
     * Updates the QueueState in statusMap to QueueState.PROCESSING with a timeout
     * @return T the candidate taken from candidateQueue
     */
    fun dequeue(): T {
        var invalid = true
        var take: T = candidateQueue.take()

        while ( invalid ) {
            val statusKey = candidateLockFunc( take )
            val currStatus = statusMap.get(statusKey)
            when( currStatus ){
                QueueState.NOT_QUEUED, // Timed out
                    QueueState.PROCESSING, // already being processed
                    null -> { // no status in the map. How did you get here?
                        logger.error("Dequeued candidate $take with QueueState $currStatus")
                        take = candidateQueue.take()
                    }
                QueueState.QUEUED -> { // continue happy path
                    invalid = false
                }
            }
        }

        statusMap.put(
                candidateLockFunc( take ),
                QueueState.PROCESSING,
                batchTimeout,
                TimeUnit.MILLISECONDS
        )
        return take
    }

    fun finishedProcessing( candidate: T ) {
        statusMap.delete( candidateLockFunc( candidate ))
    }
}

