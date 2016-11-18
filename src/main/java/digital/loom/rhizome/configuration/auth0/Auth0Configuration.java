package digital.loom.rhizome.configuration.auth0;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.kryptnostic.rhizome.configuration.annotation.ReloadableConfiguration;

/**
 * Json serializable POJO for Auth0 configuration values.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
// TODO: Implement data serializable or identified data serializable
@ReloadableConfiguration(
    uri = "auth0.yaml" )
public class Auth0Configuration implements Serializable {
    private static final long      serialVersionUID            = 3802624515206194125L;
    public static final String     DOMAIN_FIELD                = "domain";
    public static final String     ISSUER_FIELD                = "issuer";
    public static final String     CLIENT_ID_FIELD             = "clientId";
    public static final String     CLIENT_SECRET_FIELD         = "clientSecret";
    public static final String     SECURED_ROUTE_FIELD         = "securedRoute";
    public static final String     AUTHORITY_STRATEGY_FIELD    = "authorityStrategy";
    public static final String     SIGNING_ALGORITHM_FIELD     = "signingAlgorithm";
    public static final String     BASE64_ENCODED_SECRET_FIELD = "base64EncodedSecret";
    public static final String     PUBLIC_KEY_PATH_FIELD       = "publicKeyPath";
    public static final String     TOKEN_FIELD                 = "token";

    private final String           authorityStrategy;
    private final String           clientId;
    private final String           clientSecret;
    private final String           domain;
    private final String           issuer;
    private final String           securedRoute;
    private final String           signingAlgorithm;
    private final boolean          base64EncodedSecret;
    private final String           token;
    private final Optional<String> publicKeyPath;

    public Auth0Configuration(
            String domain,
            String issuer,
            String clientId,
            String clientSecret,
            String securedRoute,
            String authorityStrategy,
            String signingAlgorithm,
            boolean base64EncodedSecret,
            String token ) {
        this(
                domain,
                issuer,
                clientId,
                clientSecret,
                securedRoute,
                authorityStrategy,
                base64EncodedSecret,
                signingAlgorithm,
                Optional.fromNullable( token ),
                Optional.absent() );
    }

    @JsonCreator
    public Auth0Configuration(
            @JsonProperty( DOMAIN_FIELD ) String domain,
            @JsonProperty( ISSUER_FIELD ) String issuer,
            @JsonProperty( CLIENT_ID_FIELD ) String clientId,
            @JsonProperty( CLIENT_SECRET_FIELD ) String clientSecret,
            @JsonProperty( SECURED_ROUTE_FIELD ) String securedRoute,
            @JsonProperty( AUTHORITY_STRATEGY_FIELD ) String authorityStrategy,
            @JsonProperty( BASE64_ENCODED_SECRET_FIELD ) boolean base64EncodedSecret,
            @JsonProperty( SIGNING_ALGORITHM_FIELD ) String signingAlgorithm,
            @JsonProperty( TOKEN_FIELD ) Optional<String> token,
            @JsonProperty( PUBLIC_KEY_PATH_FIELD ) Optional<String> publicKeyPath ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( domain ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( issuer ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( clientId ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( clientSecret ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( securedRoute ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( authorityStrategy ),
                "Authority strategyic cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( signingAlgorithm ), "Signing algorithm cannot be blank" );
        this.domain = domain;
        this.issuer = issuer;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.securedRoute = securedRoute;
        this.authorityStrategy = authorityStrategy;
        this.base64EncodedSecret = base64EncodedSecret;
        this.signingAlgorithm = signingAlgorithm;
        this.token = token.or( "The token was not set. If you are expecting something here set it in auth0.yaml" );
        this.publicKeyPath = Preconditions.checkNotNull( publicKeyPath, "Public key path cannot be null." );

        Preconditions.checkState( StringUtils.isNotBlank( this.token ), "Token cannot be blank." );
    }

    @JsonProperty( DOMAIN_FIELD )
    public String getDomain() {
        return domain;
    }

    @JsonProperty( ISSUER_FIELD )
    public String getIssuer() {
        return issuer;
    }

    @JsonProperty( CLIENT_ID_FIELD )
    public String getClientId() {
        return clientId;
    }

    @JsonProperty( CLIENT_SECRET_FIELD )
    public String getClientSecret() {
        return clientSecret;
    }

    @JsonProperty( SECURED_ROUTE_FIELD )
    public String getSecuredRoute() {
        return securedRoute;
    }

    @JsonProperty( AUTHORITY_STRATEGY_FIELD )
    public String getAuthorityStrategy() {
        return authorityStrategy;
    }

    @JsonProperty( SIGNING_ALGORITHM_FIELD )
    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    @JsonProperty( BASE64_ENCODED_SECRET_FIELD )
    public boolean isBase64EncodedSecret() {
        return base64EncodedSecret;
    }

    @JsonProperty( PUBLIC_KEY_PATH_FIELD )
    public Optional<String> getPublicKeyPath() {
        return publicKeyPath;
    }

    @JsonProperty( TOKEN_FIELD )
    public String getToken() {
        return token;
    }
}
