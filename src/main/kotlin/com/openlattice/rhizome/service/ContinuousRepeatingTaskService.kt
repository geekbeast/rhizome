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
import com.openlattice.rhizome.hazelcast.ChunkedQueueSequence
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

final class ContinuousRepeatingTaskRunner<T: Any>(
        private val executor: ListeningExecutorService,
        private val candidateQueue: IQueue<T>,
        private val statusMap: IMap<T, QueueState>,
        private val task: ContinuousRepeatableTask<T>,
        parallelism: Int = Runtime.getRuntime().availableProcessors(),
        private val workerChunkSize: Int = 1
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
                            }.forEach {
                                candidateQueue.put(it)
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
        logger.info("Starting worker task for $className ")
        executor.submit {
            while ( true ) {
                try {
                    ChunkedQueueSequence(candidateQueue, workerChunkSize)
                            .map { candidateChunk ->
                                candidateChunk.filter {
                                    filterInvalidStates(it)
                                }.map { candidate ->
                                    limiter.acquire()
                                    executor.submit {
                                        try {
                                            logger.info("Operating on {}", candidate)
                                            task.operate(candidate)
                                        } catch (ex: Exception) {
                                            logger.error("Unable to operate on $candidate. ", ex)
                                        } finally {
                                            finishedProcessing(candidate)
                                            limiter.release()
                                        }
                                    }
                                }.forEach { job ->
                                    job.get(task.getTimeoutMillis(), TimeUnit.MILLISECONDS)
                                }
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
    private fun filterEnqueued( candidate: T ): Boolean {
        return statusMap.putIfAbsent(candidate, QueueState.QUEUED) != null
    }

    private fun filterInvalidStates( candidate: T ): Boolean {
        val currStatus = statusMap.get( candidate )
        when( currStatus ){
            QueueState.NOT_QUEUED, // weird state as this is never added to the statusMap
            QueueState.PROCESSING, // already being processed
            null -> { // no status in the map. How did you get here?
                return false
            }
            QueueState.QUEUED -> { // continue happy path
                statusMap.put(
                        candidate,
                        QueueState.PROCESSING,
                        task.getTimeoutMillis(),
                        TimeUnit.MILLISECONDS
                )
                return true
            }
        }
    }

    private fun finishedProcessing( candidate: T ) {
        statusMap.delete( candidate )
    }
}
