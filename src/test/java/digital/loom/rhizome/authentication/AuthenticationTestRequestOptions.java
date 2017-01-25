package digital.loom.rhizome.authentication;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class AuthenticationTestRequestOptions {
    private String usernameOrEmail;
    private String password;
    private String connection = "Tests";
    private String scope = "openid email nickname roles user_id organizations";

    @Override public boolean equals( Object o ) {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;

        AuthenticationTestRequestOptions that = (AuthenticationTestRequestOptions) o;

        if ( !usernameOrEmail.equals( that.usernameOrEmail ) )
            return false;
        if ( !password.equals( that.password ) )
            return false;
        if ( !connection.equals( that.connection ) )
            return false;
        return scope.equals( that.scope );
    }

    @Override public int hashCode() {
        int result = usernameOrEmail.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + connection.hashCode();
        result = 31 * result + scope.hashCode();
        return result;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public AuthenticationTestRequestOptions setUsernameOrEmail( String usernameOrEmail ) {
        this.usernameOrEmail = usernameOrEmail;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthenticationTestRequestOptions setPassword( String password ) {
        this.password = password;
        return this;
    }

    public String getConnection() {
        return connection;
    }

    public AuthenticationTestRequestOptions setConnection( String connection ) {
        this.connection = connection;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public AuthenticationTestRequestOptions setScope( String scope ) {
        this.scope = scope;
        return this;
    }
}
