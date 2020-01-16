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
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

final class ContinuousRepeatingTaskRunner<T: Any, K: Any>(
        private val executor: ListeningExecutorService,
        private val candidateQueue: IQueue<T>,
        private val statusMap: IMap<K, QueueState>,
        private val task: ContinuousRepeatableTask<T, K>,
        parallelism: Int = Runtime.getRuntime().availableProcessors(),
        private val fillChunkSize: Int = 1
) {
    private val limiter = Semaphore(parallelism)
    private val logger = task.getLogger()
    private val className = task.getTaskClass()

    @Suppress("UNUSED")
    private val enqueuer = if ( task.enqueuerEnabledCheck() ) {
        logger.info("Starting enqueuer task for $className ")
        executor.submit {
            while (true) {
                try {
                    task.sourceSequence()
                            .filter {
                                logger.info("Queueing candidate {}", it)
                                filterEnqueued( it )
                            }
                            .chunked(fillChunkSize)
                            .forEach {
                                candidateQueue.addAll(it)
                            }
                } catch (ex: Exception) {
                    logger.info("Encountered error while enqueuing candidates for $className task.", ex)
                }
            }
        }
    } else {
        logger.info("Skipping $className enqueue task as it is not enabled.")
        null
    }

    @Suppress("UNUSED")
    private val worker = if ( task.workerEnabledCheck() ) {
        executor.submit {
            while ( true ) {
                try {
                    generateSequence {
                        dequeue()
                    }.map { candidate ->
                        limiter.acquire()
                        executor.submit {
                            try {
                                logger.info("Operating on {}", candidate)
                                task.operate(candidate)
                            } catch (ex: Exception) {
                                logger.error("Unable to operate on $candidate. ", ex)
                            } finally {
                                finishedProcessing( candidate )
                                limiter.release()
                            }
                        }
                    }.forEach { job ->
                        job.get( task.getTimeoutMillis(), TimeUnit.MILLISECONDS )
                    }
                } catch ( ex: Exception) {
                    logger.info("Encountered error while operating on candidates.", ex )
                }
            }
        }
    } else {
        logger.info("Skipping $className worker task as it is not enabled.")
        null
    }

    /**
     * @return Boolean indicating whether candidate was successfully queued
     */
    fun filterEnqueued( candidate: T ): Boolean {
        return statusMap.putIfAbsent(task.candidateLockFunction(candidate), QueueState.QUEUED) != null
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
            val statusKey = task.candidateLockFunction( take )
            val currStatus = statusMap.get(statusKey)
            when( currStatus ){
                QueueState.NOT_QUEUED, // weird state as this is never added to the statusMap
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
                task.candidateLockFunction( take ),
                QueueState.PROCESSING,
                task.getTimeoutMillis(),
                TimeUnit.MILLISECONDS
        )
        return take
    }

    fun finishedProcessing( candidate: T ) {
        statusMap.delete( task.candidateLockFunction( candidate ))
    }
}
