package com.openlattice.rhizome.hazelcast.entryprocessors

import com.hazelcast.core.ReadOnly
import com.kryptnostic.rhizome.hazelcast.processors.AbstractRhizomeEntryProcessor

abstract class AbstractReadOnlyRhizomeEntryProcessor<K, V, R>: AbstractRhizomeEntryProcessor<K, V, R>( false ), ReadOnly
