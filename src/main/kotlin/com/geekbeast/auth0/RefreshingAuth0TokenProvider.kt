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
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class RefreshingAuth0TokenProvider internal constructor(
        private val auth0Api: AuthAPI,
        auth0Configuration: Auth0Configuration
) : Auth0TokenProvider {
    private val managementApiUrl: String = auth0Configuration.managementApiUrl
    private val logger = LoggerFactory.getLogger(RefreshingAuth0TokenProvider::class.java)

    private var tokenHolder = requestTokenHolder()
    private var needsRefreshed = false
    private var thSupplier: Supplier<TokenHolder> = memoizedSupplier()

    constructor(auth0Configuration: Auth0Configuration) : this(
            AuthAPI(
                    auth0Configuration.domain,
                    auth0Configuration.clientId,
                    auth0Configuration.clientSecret
            ), auth0Configuration
    )

    fun requestTokenHolder(): TokenHolder {
        return attempt(ExponentialBackoff(MAX_WAIT, 1.25, 2.0), 12) {
            auth0Api.requestToken(managementApiUrl).execute()
        }
    }


    /**
     * Constructs a thread safe memoized supplier that provides fresh auth0 tokens.
     *
     * If a refresh is needed we update the current token holder, update the current supplier with expiresIn from the
     * (new) current token holder.
     *
     * We always return the current token holder.
     *
     * @return A memoized supplier that returns a token holder with an unexpired token.
     */
    private fun memoizedSupplier(): Supplier<TokenHolder> {
        return Suppliers.memoizeWithExpiration(
                {
                    if (needsRefreshed) {
                        tokenHolder = requestTokenHolder()
                        thSupplier = memoizedSupplier()
                        needsRefreshed = false
                        logger.info("Refreshed auth0 token holder.")
                    } else {
                        needsRefreshed = true
                    }
                    tokenHolder
                },
                getExpirationInMillis(tokenHolder.expiresIn),
                TimeUnit.MILLISECONDS
        )
    }

    override fun getManagementApiUrl(): String {
        return managementApiUrl
    }

    override fun getToken(): String = thSupplier.get().accessToken

    companion object {
        private const val MAX_WAIT = 2 * 60 * 1000L
        private fun getExpirationInMillis(expirationInSeconds: Long): Long {
            Preconditions.checkArgument(expirationInSeconds > 0)
            return expirationInSeconds * 1000 * 4 / 5
        }
    }
}