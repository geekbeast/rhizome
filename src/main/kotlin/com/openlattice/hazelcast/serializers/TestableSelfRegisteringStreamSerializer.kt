package com.openlattice.hazelcast.serializers

import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer

interface TestableSelfRegisteringStreamSerializer<T> : SelfRegisteringStreamSerializer<T> {

    fun generateTestValue(): T

}