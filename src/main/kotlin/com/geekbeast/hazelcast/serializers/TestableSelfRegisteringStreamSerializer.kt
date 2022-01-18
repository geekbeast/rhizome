package com.geekbeast.hazelcast.serializers

import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer

interface TestableSelfRegisteringStreamSerializer<T> : SelfRegisteringStreamSerializer<T> {

    fun generateTestValue(): T

}