package com.geekbeast.util

import com.google.common.base.Stopwatch
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(StopWatch::class.java)

class StopWatch(val log : String) : AutoCloseable {
    override fun close() {
        logger.info("$log took ${sw.elapsed(TimeUnit.MILLISECONDS)} ms.")
    }

    private val sw = Stopwatch.createStarted()

}