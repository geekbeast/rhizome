package com.geekbeast.hazelcast

import com.kryptnostic.rhizome.configuration.hazelcast.HazelcastConfiguration

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class HazelcastClientProvider(val clients : Map<String, HazelcastConfiguration>) {
    init {
        check( clients.values.all { !it.isServer } ) {"Cannot specify server configuration for clients"}
    }
}