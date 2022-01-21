package com.geekbeast.rhizome

/**
 * @author Drew Bailey &lt;drew@openlattice.com&gt;
 */
class KotlinDelegatedStringSet( strings: Set<String> ): Set<String> by strings
