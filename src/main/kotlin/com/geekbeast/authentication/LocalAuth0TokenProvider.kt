/*
 * Copyright (C) 2019. OpenLattice, Inc.
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
 *
 */

package com.geekbeast.authentication

import com.auth0.client.auth.AuthAPI
import com.auth0.exception.Auth0Exception
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import com.openlattice.auth0.Auth0TokenProvider
import com.openlattice.authentication.Auth0Configuration
import java.util.concurrent.TimeUnit

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class LocalAuth0TokenProvider(auth0Configuration: Auth0Configuration) : Auth0TokenProvider {
    private val tokenUpdater: Supplier<String>
    private var token: java.util.function.Supplier<String>? = null
    override fun getManagementApiUrl(): String {
        return managementApiUrl
    }

    fun getTokenUpdater(): Supplier<String> {
        return tokenUpdater
    }

    override fun getToken(): String {
        return token!!.get()
    }

    companion object {
        private const val RETRY_MILLIS = 30000
    }

    init {
        managementApiUrl = auth0Configuration.managementApiUrl
        tokenUpdater = label@ Supplier {
            try {
                val holder = auth0Api.requestToken(
                        managementApiUrl
                ).execute()
                val expiresInMillis = holder.expiresIn * 1000 / 2
                token = Suppliers.memoizeWithExpiration(
                        getTokenUpdater(),
                        expiresInMillis,
                        TimeUnit.MILLISECONDS
                )
                return@label holder.accessToken
            } catch (e: Auth0Exception) {
                token = Suppliers.memoizeWithExpiration(
                        getTokenUpdater(),
                        RETRY_MILLIS.toLong(),
                        TimeUnit.MILLISECONDS
                )
                return@label ""
            }
        }
        // kick off the initial token request
        tokenUpdater.get()
    }
}
