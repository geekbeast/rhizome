package com.geekbeast.rhizome.core

import org.junit.Test

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class StandaloneRhizomeServerTest {
    @Test
    fun startServer() {
        val srs = StandaloneRhizomeServer()
        srs.start()
    }

}