package com.geekbeast.auth0

import com.auth0.client.mgmt.ManagementAPI
import com.geekbeast.authentication.Auth0Configuration

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class ManagementApiProvider(
        private val tokenProvider: Auth0TokenProvider,
        private val auth0Configuration: Auth0Configuration
) {
    fun getInstance(): ManagementAPI = ManagementAPI(auth0Configuration.domain, tokenProvider.token)
}