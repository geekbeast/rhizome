package com.openlattice.rhizome

import java.util.UUID

/**
 * @author Drew Bailey &lt;drew@openlattice.com&gt;
 */
class KotlinDelegatedUUIDSet ( uuids: Set<UUID> ): Set<UUID> by uuids
