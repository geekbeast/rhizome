/*
 * Copyright (C) 2017. OpenLattice, Inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 */
package com.geekbeast.auth0

import com.auth0.client.auth.AuthAPI
import com.geekbeast.authentication.Auth0Configuration
import com.auth0.json.auth.TokenHolder
import com.geekbeast.util.ExponentialBackoff
import com.geekbeast.util.attempt
import com.google.common.base.Preconditions
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import org.apache.commons.lang3.NotImplementedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class EmptyAuth0TokenProvider internal constructor(
    private val auth0Api: AuthAPI,
    auth0Configuration: Auth0Configuration,
) : Auth0TokenProvider {
    private val managementApiUrl: String = auth0Configuration.managementApiUrl
    private val logger = LoggerFactory.getLogger(EmptyAuth0TokenProvider::class.java)


    constructor(auth0Configuration: Auth0Configuration) : this(
        AuthAPI(
            auth0Configuration.domain,
            auth0Configuration.clientId,
            auth0Configuration.clientSecret
        ), auth0Configuration
    )

    override fun getManagementApiUrl(): String {
        return managementApiUrl
    }

    override fun getToken(): String = throw NotImplementedException("This shouldn't be getting called.")

}