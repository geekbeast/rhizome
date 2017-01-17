package digital.loom.rhizome.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.jwt.Algorithm;
import com.auth0.spring.security.api.Auth0AuthenticationProvider;
import com.auth0.spring.security.api.authority.AuthorityStrategy;

public class ConfigurableAuth0AuthenticationProvider extends Auth0AuthenticationProvider {

    private final AuthenticationAPIClient auth0Client;

    public ConfigurableAuth0AuthenticationProvider( AuthenticationAPIClient auth0Client ) {
        this.auth0Client = auth0Client;
    }

    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException {
        return super.authenticate( authentication );
        //TODO: Pretty sure we can skip below, now the we are correctly configuring scope options
        //Need to verify that front-end code is correctly passing roles before removing this.
//        final Auth0JWTToken tokenAuth = ( (Auth0JWTToken) authentication );
//        UserProfile profile = auth0Client.tokenInfo( tokenAuth.getJwt() ).execute();
//        @SuppressWarnings( "unchecked" )
//        Map<String, Object> decoded = (Map<String, Object>) tokenAuth.getDetails();
        
//        decoded.putAll( profile.getAppMetadata() );
//        tokenAuth.setPrincipal( new Auth0UserDetails( decoded, getAuthorityStrategy() ) );
//        tokenAuth.setDetails( decoded );
        
//        return authentication;
    }

    @Override
    public void setAuthorityStrategy( AuthorityStrategy authorityStrategy ) {
        super.setAuthorityStrategy( authorityStrategy );
    }

    @Override
    public void setBase64EncodedSecret( boolean base64EncodedSecret ) {
        super.setBase64EncodedSecret( base64EncodedSecret );
    }

    @Override
    public void setClientId( String clientId ) {
        super.setClientId( clientId );
    }

    @Override
    public void setClientSecret( String clientSecret ) {
        super.setClientSecret( clientSecret );
    }

    @Override
    public void setDomain( String domain ) {
        super.setDomain( domain );
    }

    @Override
    public void setIssuer( String issuer ) {
        super.setIssuer( issuer );
    }

    @Override
    public void setPublicKeyPath( String publicKeyPath ) {
        super.setPublicKeyPath( publicKeyPath );
    }

    @Override
    public void setSecuredRoute( String securedRoute ) {
        super.setSecuredRoute( securedRoute );
    }

    @Override
    public void setSigningAlgorithm( Algorithm signingAlgorithm ) {
        super.setSigningAlgorithm( signingAlgorithm );
    }
}
