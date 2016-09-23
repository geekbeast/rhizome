package digital.loom.rhizome.authentication;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.auth0.Auth0;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Credentials;
import com.auth0.request.AuthenticationRequest;

/**
 * This class is a sanity check to ensure that authentication is successfully working against auth0 server. The hard
 * coded credentials and secrets are only usable for testing.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class AuthenticationTest {

    @Test
    public void testLogin() {
        Credentials creds = authenticate();

        Assert.assertTrue( StringUtils.isNotBlank( creds.getIdToken() ) );
    }

    public static Credentials authenticate() {
        Auth0 auth0 = new Auth0( "PTmyExdBckHAiyOjh4w2MqSIUGWWEdf8", "loom.auth0.com" );
        AuthenticationAPIClient client = auth0.newAuthenticationAPIClient();

        AuthenticationRequest request = client.login( "support@kryptnostic.com", "abracadabra" )
                .setConnection( "Tests" );
        return request.execute();
    }
}
