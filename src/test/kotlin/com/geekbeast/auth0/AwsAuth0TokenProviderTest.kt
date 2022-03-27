package com.geekbeast.auth0

import com.auth0.client.auth.AuthAPI
import com.auth0.json.auth.TokenHolder
import com.auth0.net.AuthRequest
import com.auth0.net.TokenRequest
import com.geekbeast.authentication.Auth0Configuration
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class AwsAuth0TokenProviderTest {
    @Test(timeout = 3000)
    // timeout means deadlock in something
    fun testMemoization() {
        val managementApiUrl =  RandomStringUtils.random(10)
        val audience = RandomStringUtils.random(10)
        val auth0Api = Mockito.mock(AuthAPI::class.java)
        val authRequest = Mockito.mock(TokenRequest::class.java)

        Mockito.`when`(authRequest.execute()).then { tokenHolder() }
        Mockito.`when`(auth0Api.requestToken(managementApiUrl)).thenReturn(authRequest)
        val exec = Executors.newCachedThreadPool()
        // construct the token provider on another thread in case there's a bug.
        val tokenProvider = exec.submit(Callable<RefreshingAuth0TokenProvider> {
            RefreshingAuth0TokenProvider(
                auth0Api, Auth0Configuration(
                    audience,
                    RandomStringUtils.random(10),
                    RandomStringUtils.random(10),
                    setOf(),
                    Optional.empty(),
                    managementApiUrl
                )
            )
        }).get()
        exec.shutdownNow()

        val v1 = tokenProvider.token
        val v2 = tokenProvider.token
        Thread.sleep(2100)
        val v3 = tokenProvider.token

        Assert.assertTrue( v1==v2 )
        Assert.assertTrue( v1!=v3 )
    }

    fun tokenHolder() : TokenHolder =TokenHolder(
            RandomStringUtils.randomAlphanumeric(10),
            "",
            "",
            "",
            2,
            "",
            Date(Date().toInstant().plusSeconds(2).toEpochMilli())
    )
}
