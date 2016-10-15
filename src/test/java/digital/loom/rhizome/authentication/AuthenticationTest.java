package digital.loom.rhizome.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger                  logger              = LoggerFactory
            .getLogger( AuthenticationTest.class );
    private static final String                  domain              = "loom.auth0.com";
    private static final String                  issuer              = "https://loom.auth0.com/";
    private static final String                  clientId            = "PTmyExdBckHAiyOjh4w2MqSIUGWWEdf8";
    private static final String                  clientSecret        = "VmzAkSSsYQe7DGe5Fz8IHZnKsZF8Ul3UA6tMtikZQC1wLxoA-Krve0bdMN2UH1jb";
    private static final String                  securedRoute        = "NOT_USED";
    private static final String                  authorityStrategy   = "ROLES";
    private static final String                  signingAlgorithm    = "HS256";
    // private static final String defaultAuth0ApiSecurityEnabled = "false";
    // private static final String signingAlgorithm = "HS256";
    private static final boolean                 base64EncodedSecret = true;
    public static final Auth0Configuration       configuration       = new Auth0Configuration(
            domain,
            issuer,
            clientId,
            clientSecret,
            securedRoute,
            authorityStrategy,
            signingAlgorithm,
            base64EncodedSecret );
    private static final Auth0                   auth0               = new Auth0(
            "PTmyExdBckHAiyOjh4w2MqSIUGWWEdf8",
            "loom.auth0.com" );
    private static final AuthenticationAPIClient client              = auth0.newAuthenticationAPIClient();

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
        logger.info( "Roles: {}", userDetails.getAuthorities() );
        // // First check the authority strategy configured for the API
        // if ( !Auth0AuthorityStrategy.contains( configuration.getAuthorityStrategy() ) ) {
        // throw new IllegalStateException( "Configuration error, illegal authority strategy" );
        // }
        // final Auth0AuthorityStrategy authorityStrategy = Auth0AuthorityStrategy
        // .valueOf( configuration.getAuthorityStrategy() );
        // final Auth0AuthenticationProvider authenticationProvider = new Auth0AuthenticationProvider();
        // authenticationProvider.setDomain( configuration.getDomain() );
        // authenticationProvider.setIssuer( configuration.getIssuer() );
        // authenticationProvider.setClientId( configuration.getClientId() );
        // authenticationProvider.setClientSecret( configuration.getClientSecret() );
        // authenticationProvider.setSecuredRoute( configuration.getSecuredRoute() );
        // authenticationProvider.setAuthorityStrategy( authorityStrategy );
        // authenticationProvider.setBase64EncodedSecret( configuration.isBase64EncodedSecret() );
        //
        // authenticationProvider.afterPropertiesSet();
        // Auth0JWTToken authentication = (Auth0JWTToken) authenticationProvider
        // .authenticate( );
        // System.out.println( authentication.getAuthorities().toString() );

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

    public static Pair<Credentials, Authentication> authenticate() {

        AuthenticationRequest request = client.login( "support@kryptnostic.com", "abracadabra" )
                .setConnection( "Tests" );

        return Pair.of( request.execute(), client.getProfileAfter( request ).execute() );
    }
}
