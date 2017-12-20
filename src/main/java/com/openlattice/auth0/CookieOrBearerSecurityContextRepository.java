/*
 * Copyright (C) 2017. OpenLattice, Inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 */

package com.openlattice.auth0;

import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class CookieOrBearerSecurityContextRepository extends BearerSecurityContextRepository {
    private final static Logger logger               = LoggerFactory
            .getLogger( CookieOrBearerSecurityContextRepository.class );
    //Header name is case-insensitive so we use same for consistency with front-end
    private final static String AUTHORIZATION_HEADER = "Authorization";
    private final static String AUTHORIZATION_COOKIE = AUTHORIZATION_HEADER.toLowerCase();

    @Override
    public SecurityContext loadContext( HttpRequestResponseHolder requestResponseHolder ) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String token = tokenFromRequest( requestResponseHolder.getRequest() );
        Authentication authentication = new WrappedPreAuthenticatedAuthenticationJwt(
                PreAuthenticatedAuthenticationJsonWebToken.usingToken( token ) );
        if ( authentication != null ) {
            context.setAuthentication( authentication );
            logger.debug( "Found bearer token in request. Saving it in SecurityContext" );
        }
        return context;
    }

    @Override
    public void saveContext( SecurityContext context, HttpServletRequest request, HttpServletResponse response ) {
    }

    @Override
    public boolean containsContext( HttpServletRequest request ) {
        return tokenFromRequest( request ) != null;
    }

    private String tokenFromRequest( HttpServletRequest request ) {
        //        final String value = request.getHeader( "Authorization" );
        //
        //        if ( value == null || !value.toLowerCase().startsWith( "bearer" ) ) {
        //            return null;
        //        }
        //
        //        String[] parts = value.split( " " );
        //
        //        if ( parts.length < 2 ) {
        //            return null;
        //        }
        //
        //        return parts[ 1 ].trim();
        //
        //        final String authorizationHeader = httpRequest.getHeader( "authorization" );
        //
        //        if ( authorizationHeader == null && authorizationCookie == null ) {
        //            return null;
        //        }

        final String authorizationInfo;
        final String authorizationHeader = getAuthorizationTokenFromHeader( request );

        if ( authorizationHeader != null ) {
            authorizationInfo = authorizationHeader;
        } else {
            authorizationInfo = getAuthorizationTokenFromCookie( request );
        }

        if ( authorizationInfo == null || !authorizationInfo.toLowerCase().startsWith( "bearer" ) ) {
            return null;
        }

        //Since " " is a single character string it is optimized to not use a pattern.
        final String[] parts = authorizationInfo.split( " " );

        if ( parts.length != 2 ) {
            // "Unauthorized: Format is Authorization: Bearer [token]"
            return null;
        }

        //        final String scheme = parts[ 0 ];
        //        final String credentials = parts[ 1 ];

        return parts[ 1 ];
    }

    private static String getAuthorizationTokenFromCookie( HttpServletRequest request ) {
        Cookie[] cookies = request.getCookies();
        if ( cookies != null ) {
            for ( Cookie cookie : request.getCookies() ) {
                if ( StringUtils.equals( cookie.getName(), AUTHORIZATION_COOKIE ) ) {
                    try {
                        return URLDecoder.decode( cookie.getValue(), StandardCharsets.UTF_8.name() );
                    } catch ( UnsupportedEncodingException e ) {
                        logger.error( "Unable to decode authorization cookie. " );
                    }
                }
            }
        }
        return null;
    }

    private static String getAuthorizationTokenFromHeader( HttpServletRequest request ) {
        return request.getHeader( AUTHORIZATION_HEADER );
    }
}
