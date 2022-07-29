package com.geekbeast.hazelcast

import com.zaxxer.hikari.HikariDataSource

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@getmethodic.com&gt;
 */
interface PreHazelcastUpgradeService {
    fun runUpgrade()
}

class NoOpPreHazelcastUpgradeService() : PreHazelcastUpgradeService {
    override fun runUpgrade() {
    }
}