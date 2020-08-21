package com.openlattice.auth0

import com.auth0.client.auth.AuthAPI
import com.auth0.exception.Auth0Exception
import com.google.common.annotations.VisibleForTesting
import com.openlattice.authentication.Auth0Configuration

class Auth0Delegate
@JvmOverloads
private constructor(
        val auth0domain: String,
        val auth0clientId: String,
        val auth0Connection: String = DEFAULT_AUTH0_CONNECTION,
        val auth0Scopes: String = DEFAULT_AUTH0_SCOPES ,
        val auth0Api: AuthAPI = AuthAPI(auth0domain, auth0clientId, "")
) {
    private constructor(
            config: Auth0Configuration
    ): this( config.domain, config.clientId )

    companion object {
        private const val DEFAULT_AUTH0_CONNECTION = "Username-Password-Authentication"
        private const val DEFAULT_AUTH0_SCOPES = "openid email nickname roles user_id organizations"

        @JvmStatic
        @VisibleForTesting
        fun fromConfig(config: Auth0Configuration ): Auth0Delegate {
            return Auth0Delegate( config )
        }

        @JvmStatic
        @VisibleForTesting
        fun fromConstants(domain: String, clientId: String, auth0Connection: String, auth0Scopes: String): Auth0Delegate {
            return Auth0Delegate( domain, clientId, auth0Connection, auth0Scopes)
        }
    }

    @VisibleForTesting
    @Throws(Auth0Exception::class)
    fun getIdToken(username: String, password: String ): String {
        return getIdToken(auth0Connection, username, password)
    }

    @VisibleForTesting
    @Throws(Auth0Exception::class)
    fun getIdToken(realm: String, username: String, password: String): String {
        return auth0Api
                .login(username, password.toCharArray(), realm)
                .setScope(auth0Scopes)
                .setAudience("https://api.openlattice.com")
                .execute()
                .idToken
    }

    @VisibleForTesting
    @Throws(Auth0Exception::class)
    fun getAccessToken(realm: String, username: String, password: String): String {
        return auth0Api
                .login(username, password.toCharArray(), realm)
                .setScope(auth0Scopes)
                .setAudience("https://api.openlattice.com")
                .execute()
                .accessToken
    }
}
