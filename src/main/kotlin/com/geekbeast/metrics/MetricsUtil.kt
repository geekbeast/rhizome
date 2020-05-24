package com.geekbeast.metrics

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.codahale.metrics.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extension functions for metric registry to make it easy to time and log operations.
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
private val loggers = mutableMapOf<Class<*>, Logger>()

fun <R> MetricRegistry.time(
        clazz: Class<*>,
        vararg names: String,
        op: (Logger, Timer.Context) -> R): R {

    val timer = timer(name(clazz, *names))
    val context = timer.time()
    return try {
        val logger = loggers.getOrPut(clazz) { LoggerFactory.getLogger(clazz) }
        val result = op(logger, context)
        result
    } finally {
        context.stop()
    }
}

fun <R> MetricRegistry.meter(
        clazz: Class<*>,
        vararg names: String,
        count: Long = 0L,
        op: (Logger) -> R
): R {
    val timer = meter(name(clazz, *names))
    return try {
        val logger = loggers.getOrPut(clazz) { LoggerFactory.getLogger(clazz) }
        val result = op(logger)
        result
    } finally {
        if (count == 0L) {
            timer.mark()
        } else {
            timer.mark(count)
        }
    }
}