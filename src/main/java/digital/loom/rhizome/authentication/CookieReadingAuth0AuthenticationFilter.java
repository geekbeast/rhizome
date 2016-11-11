package digital.loom.rhizome.authentication;

import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;
import com.auth0.spring.security.api.Auth0AuthenticationFilter;
import com.google.common.base.MoreObjects;

public class CookieReadingAuth0AuthenticationFilter extends Auth0AuthenticationFilter {
    @Override
    protected String getToken( HttpServletRequest httpRequest ) {
        String authorizationCookie = null;
        Cookie[] cookies = httpRequest.getCookies();
        if ( cookies != null ) {
            for ( Cookie cookie : httpRequest.getCookies() ) {
                if ( StringUtils.equals( cookie.getName(), "authorization" ) ) {
                    authorizationCookie = cookie.getValue();
                    break;
                }
            }
        }

        final String authorizationHeader = httpRequest.getHeader( "authorization" );

        final String[] parts = MoreObjects.firstNonNull( authorizationHeader, authorizationCookie ).split( " " );
        if ( parts.length != 2 ) {
            // "Unauthorized: Format is Authorization: Bearer [token]"
            return null;
        }
        final String scheme = parts[ 0 ];
        final String credentials = parts[ 1 ];
        final Pattern pattern = Pattern.compile( "^Bearer$", Pattern.CASE_INSENSITIVE );
        return pattern.matcher( scheme ).matches() ? credentials : null;
    }
}
