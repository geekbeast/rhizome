package com.openlattice.rhizome.service

import org.slf4j.Logger

interface ContinuousRepeatableTask<T> {

    fun enqueuerEnabledCheck(): Boolean

    fun workerEnabledCheck(): Boolean

    fun operate(candidate: T)

    fun sourceSequence() : Sequence<T>

    fun getLogger(): Logger

    fun getTimeoutMillis() : Long

    fun getTaskClass() : Class<Any>
}