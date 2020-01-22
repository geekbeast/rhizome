package com.openlattice.auth0

import com.auth0.client.auth.AuthAPI
import com.auth0.json.auth.TokenHolder
import com.auth0.net.AuthRequest
import com.openlattice.authentication.Auth0Configuration
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert
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
        Mockito.`when`(tokenHolder.expiresIn).thenReturn(60 )
        Mockito.`when`(authRequest.execute()).thenReturn(tokenHolder)
        Mockito.`when`(auth0Api.requestToken(managementApiUrl)).thenReturn(authRequest)

        val tokenProvider = AwsAuth0TokenProvider(auth0Api, Auth0Configuration(
                audience,
                RandomStringUtils.random(10),
                RandomStringUtils.random(10),
                setOf(),
                Optional.empty(),
                managementApiUrl
        ))

        val v1 = tokenProvider.token
        val v2 = tokenProvider.token
        val v3 = tokenProvider.token
        Thread.sleep(200);
        val v4 = tokenProvider.token
        val v5 = tokenProvider.token

        Assert.assertTrue( v1==v2 )
        Assert.assertTrue( v1!=v3 )
    }


}