package com.geekbeast.configuration.hazelcast

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
data class DurableExecutorConfiguration(
        val name: String,
        @JsonProperty("pool-size") val poolSize: Int,
        val durability: Int,
        val capacity: Int,
        val splitBrainProtectionName: String = ""
)