package com.openlattice.auth0

import com.auth0.client.auth.AuthAPI
import com.auth0.json.auth.TokenHolder
import com.auth0.net.AuthRequest
import com.openlattice.authentication.Auth0Configuration
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Test
import org.mockito.Mockito
import java.util.*

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class AwsAuth0TokenProviderTest {
    @Test
    fun testMemoization() {
        val managementApiUrl =  RandomStringUtils.random(10)
        val audience = RandomStringUtils.random(10)
        val auth0Api = Mockito.mock(AuthAPI::class.java)
        val authRequest = Mockito.mock(AuthRequest::class.java)
        val tokenHolder = Mockito.mock(TokenHolder::class.java)

        Mockito.`when`(tokenHolder.accessToken).then { RandomStringUtils.random(10) }
        Mockito.`when`(authRequest.execute()).thenReturn(tokenHolder)
        Mockito.`when`(auth0Api.requestToken(audience)).thenReturn(authRequest)

        val tokenProvider = AwsAuth0TokenProvider(auth0Api, Auth0Configuration(
                audience,
                RandomStringUtils.random(10),
                RandomStringUtils.random(10),
                setOf(),
                Optional.empty(),
                managementApiUrl
        ))
    }


}