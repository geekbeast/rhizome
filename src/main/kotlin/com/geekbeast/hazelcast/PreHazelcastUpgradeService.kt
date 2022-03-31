package com.geekbeast.hazelcast

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