package digital.loom.rhizome.configuration.auth0;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Json serializable POJO for Auth0 configuration values.
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 *
 */
public class Auth0Configuration {
    public static final String     DOMAIN_FIELD                = "domain";
    public static final String     ISSUER_FIELD                = "issuer";
    public static final String     CLIENT_ID_FIELD             = "clientId";
    public static final String     CLIENT_SECRET_FIELD         = "clientSecret";
    public static final String     SECURED_ROUTE_FIELD         = "securedRoute";
    public static final String     AUTHORITY_STRATEGY_FIELD    = "authorityStrategy";
    public static final String     BASE64_ENCODED_SECRET_FIELD = "base64EncodedSecret";
    public static final String     PUBLIC_KEY_PATH_FIELD       = "publicKeyPath";

    private final String           domain;
    private final String           issuer;
    private final String           clientId;
    private final String           clientSecret;
    private final String           securedRoute;
    private final String           authorityStrategy;
    private final boolean          base64EncodedSecret;
    private final Optional<String> publicKeyPath;

    public Auth0Configuration(
            String domain,
            String issuer,
            String clientId,
            String clientSecret,
            String securedRoute,
            String authorityStrategy,
            boolean base64EncodedSecret ) {
        this(
                domain,
                issuer,
                clientId,
                clientSecret,
                securedRoute,
                authorityStrategy,
                base64EncodedSecret,
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
            @JsonProperty( PUBLIC_KEY_PATH_FIELD ) Optional<String> publicKeyPath ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( domain ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( issuer ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( clientId ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( clientSecret ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( securedRoute ), "Domain cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( authorityStrategy ), "Domain cannot be blank" );
        this.domain = domain;
        this.issuer = issuer;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.securedRoute = securedRoute;
        this.authorityStrategy = authorityStrategy;
        this.base64EncodedSecret = base64EncodedSecret;
        this.publicKeyPath = Preconditions.checkNotNull( publicKeyPath, "Public key path cannot be null." );
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

    @JsonProperty( BASE64_ENCODED_SECRET_FIELD )
    public boolean isBase64EncodedSecret() {
        return base64EncodedSecret;
    }

    @JsonProperty( PUBLIC_KEY_PATH_FIELD )
    public Optional<String> getPublicKeyPath() {
        return publicKeyPath;
    }
}
