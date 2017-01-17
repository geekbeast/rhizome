package digital.loom.rhizome.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.auth0.Auth0;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Authentication;
import com.auth0.authentication.result.Credentials;
import com.auth0.authentication.result.UserProfile;
import com.auth0.jwt.JWTVerifier;
import com.auth0.request.AuthenticationRequest;
import com.auth0.spring.security.api.Auth0AuthorityStrategy;
import com.auth0.spring.security.api.Auth0JWTToken;
import com.auth0.spring.security.api.Auth0UserDetails;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableList;

import digital.loom.rhizome.configuration.auth0.Auth0Configuration;

/**
 * This class is a sanity check to ensure that authentication is successfully working against auth0 server. The hard
 * coded credentials and secrets are only usable for testing.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class AuthenticationTest {
    private static final Logger                            logger              = LoggerFactory
            .getLogger( AuthenticationTest.class );
    private static final String                            domain              = "loom.auth0.com";
    private static final String                            issuer              = "https://loom.auth0.com/";
    private static final String                            clientId            = "PTmyExdBckHAiyOjh4w2MqSIUGWWEdf8";
    private static final String                            clientSecret        = "VmzAkSSsYQe7DGe5Fz8IHZnKsZF8Ul3UA6tMtikZQC1wLxoA-Krve0bdMN2UH1jb";
    private static final String                            securedRoute        = "NOT_USED";
    private static final String                            authorityStrategy   = "ROLES";
    private static final String                            signingAlgorithm    = "HS256";
    // private static final String defaultAuth0ApiSecurityEnabled = "false";
    // private static final String signingAlgorithm = "HS256";
    private static final boolean                           base64EncodedSecret = true;
    private static final Pair<Credentials, Authentication> cache;
    private static final String                  token = "No token for you";
    public static final Auth0Configuration                 configuration       = new Auth0Configuration(
            domain,
            issuer,
            clientId,
            clientSecret,
            securedRoute,
            authorityStrategy,
            signingAlgorithm,
            base64EncodedSecret,
            token );

    private static final Auth0                             auth0               = new Auth0(
            clientId,
            "loom.auth0.com" );
    private static final AuthenticationAPIClient           client              = auth0.newAuthenticationAPIClient();

    static {
        AuthenticationRequest request = client.login( "support@kryptnostic.com", "abracadabra" )
                .setConnection( "Tests" )
                .setScope( "openid email nickname roles user_id" );
        cache = Pair.of( request.execute(), client.getProfileAfter( request ).execute() );
        logger.info( "Using the following idToken: Bearer {}" , cache.getRight().getCredentials().getIdToken() );
    }

    @Test
    public void testRoles() throws Exception {
        Pair<Credentials, Authentication> auth = authenticate();
        JWTVerifier jwtVerifier = new JWTVerifier( new Base64( true ).decodeBase64( clientSecret ), clientId, issuer );
        Auth0JWTToken token = new Auth0JWTToken( auth.getLeft().getIdToken() );
        final Map<String, Object> decoded = jwtVerifier.verify( auth.getLeft().getIdToken() );
        Map<String, Object> d2 = auth.getRight().getProfile().getAppMetadata();
        Auth0UserDetails userDetails = new Auth0UserDetails(
                d2,
                Auth0AuthorityStrategy.valueOf( authorityStrategy ).getStrategy() );
        Assert.assertTrue( "Return roles must contain user",
                userDetails.getAuthorities().contains( new SimpleGrantedAuthority( "user" ) ) );
        logger.info( "Roles: {}", userDetails.getAuthorities() );
    }

    @Test
    public void testLogin() throws JsonParseException, JsonMappingException, IOException {
        Credentials creds = authenticate().getLeft();
        UserProfile profile = client.tokenInfo( creds.getIdToken() ).execute();

        @SuppressWarnings( "unchecked" )
        List<String> roles = (List<String>) profile.getAppMetadata().getOrDefault( "roles", ImmutableList.of() );
        Assert.assertTrue( "Return roles must contain user", roles.contains( "user" ) );
        Assert.assertTrue( StringUtils.isNotBlank( creds.getIdToken() ) );
    }

    public static synchronized Pair<Credentials, Authentication> authenticate() {
        return cache;
    }
}
