package digital.loom.rhizome.authentication;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.Charsets;

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
                    try {
                        authorizationCookie = URLDecoder.decode( cookie.getValue() ,Charsets.UTF_8.name() );
                    } catch ( UnsupportedEncodingException e ) {
                        logger.error( "Unable to decode authorization cookie. " );
                    }
                    break;
                }
            }
        }

        final String authorizationHeader = httpRequest.getHeader( "authorization" );
        
        if ( authorizationHeader == null && authorizationCookie == null ) {
            return null;
        }
        
        final String authorizationInfo = MoreObjects.firstNonNull( authorizationHeader, authorizationCookie );
        if ( authorizationInfo == null ) {
            return null;
        }
        final String[] parts = authorizationInfo.split( "(?:%20|\\s)+" );

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
