package digital.loom.rhizome.authentication;

import com.auth0.Auth0;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Authentication;
import com.auth0.authentication.result.Credentials;
import com.auth0.authentication.result.UserProfile;
import com.auth0.jwt.JWTVerifier;
import com.auth0.spring.security.api.Auth0AuthorityStrategy;
import com.auth0.spring.security.api.Auth0JWTToken;
import com.auth0.spring.security.api.Auth0UserDetails;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import digital.loom.rhizome.configuration.auth0.Auth0Configuration;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class is a sanity check to ensure that authentication is successfully working against auth0 server. The hard
 * coded credentials and secrets are only usable for testing.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class AuthenticationTest {
    private static final Logger  logger              = LoggerFactory
            .getLogger( AuthenticationTest.class );
    private static final String  domain              = "loom.auth0.com";
    private static final String  issuer              = "https://loom.auth0.com/";
    private static final String  clientId            = "PTmyExdBckHAiyOjh4w2MqSIUGWWEdf8";
    private static final String  clientSecret        = "VmzAkSSsYQe7DGe5Fz8IHZnKsZF8Ul3UA6tMtikZQC1wLxoA-Krve0bdMN2UH1jb";
    private static final String  securedRoute        = "NOT_USED";
    private static final String  authorityStrategy   = "ROLES";
    private static final String  signingAlgorithm    = "HS256";
    // private static final String defaultAuth0ApiSecurityEnabled = "false";
    // private static final String signingAlgorithm = "HS256";
    private static final boolean base64EncodedSecret = true;
    private static final LoadingCache<AuthenticationTestRequestOptions, Authentication> authentications;
    private static final AuthenticationTestRequestOptions authOptions   = new AuthenticationTestRequestOptions()
            .setUsernameOrEmail( "support@kryptnostic.com" )
            .setPassword( "abracadabra" );
    private static final String                           token         = "No token for you";
    public static final  Auth0Configuration               configuration = new Auth0Configuration(
            domain,
            issuer,
            clientId,
            clientSecret,
            securedRoute,
            authorityStrategy,
            signingAlgorithm,
            base64EncodedSecret,
            token );

    private static final Auth0                   auth0  = new Auth0(
            clientId,
            "loom.auth0.com" );
    private static final AuthenticationAPIClient client = auth0.newAuthenticationAPIClient();

    static {
        authentications = CacheBuilder.newBuilder()
                .build( new CacheLoader<AuthenticationTestRequestOptions, Authentication>() {
                    @Override public Authentication load( AuthenticationTestRequestOptions options ) throws Exception {
                        Authentication auth = client
                                .getProfileAfter( client.login( options.getUsernameOrEmail(), options.getPassword() )
                                        .setConnection( options.getConnection() )
                                        .setScope( options.getScope() ) )
                                .execute();
                        logger.info( "Caching the following idToken: Bearer {}", auth.getCredentials().getIdToken() );
                        return auth;
                    }
                } );
    }

    public static Authentication getAuthentication( AuthenticationTestRequestOptions options ) {
        return authentications.getUnchecked( options );
    }

    public static Authentication authenticate() {
        return authentications.getUnchecked( authOptions );
    }

    @Test
    public void testRoles() throws Exception {
        Authentication auth = authenticate();
        JWTVerifier jwtVerifier = new JWTVerifier( new Base64( true ).decodeBase64( clientSecret ), clientId, issuer );
        String idToken = auth.getCredentials().getIdToken();
        Auth0JWTToken token = new Auth0JWTToken( idToken );
        final Map<String, Object> decoded = jwtVerifier.verify( idToken );
        Map<String, Object> d2 = auth.getProfile().getAppMetadata();

        Auth0UserDetails userDetails = new Auth0UserDetails(
                d2,
                Auth0AuthorityStrategy.ROLES.getStrategy() );

        Assert.assertTrue( "Return roles must contain user",
                userDetails.getAuthorities().contains( new SimpleGrantedAuthority( "user" ) ) );
        logger.info( "Roles: {}", userDetails.getAuthorities() );
    }

    @Test
    public void testLogin() throws JsonParseException, JsonMappingException, IOException {
        Credentials creds = authenticate().getCredentials();
        UserProfile profile = client.tokenInfo( creds.getIdToken() ).execute();

        @SuppressWarnings( "unchecked" )
        List<String> roles = (List<String>) profile.getAppMetadata().getOrDefault( "roles", ImmutableList.of() );
        Assert.assertTrue( "Return roles must contain user", roles.contains( "user" ) );
        Assert.assertTrue( StringUtils.isNotBlank( creds.getIdToken() ) );
    }
}
