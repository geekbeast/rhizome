package com.geekbeast.rhizome.core

import com.kryptnostic.rhizome.configuration.websockets.BaseRhizomeServer
import com.kryptnostic.rhizome.pods.JettyContainerPod

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class StandaloneRhizomeServer(vararg pods:  Class<*>) : BaseRhizomeServer()


