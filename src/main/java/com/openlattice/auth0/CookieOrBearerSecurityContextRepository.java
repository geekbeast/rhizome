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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class CookieOrBearerSecurityContextRepository extends BearerSecurityContextRepository {

    private final static Logger logger = LoggerFactory.getLogger( CookieOrBearerSecurityContextRepository.class );

    //Header name is case-insensitive so we use same for consistency with front-end
    private final static String AUTHORIZATION_HEADER = "Authorization";
    private final static String AUTHORIZATION_COOKIE = AUTHORIZATION_HEADER.toLowerCase();
    private final static String BEARER_PREFIX        = "Bearer";
    private final static String CSRF_COOKIE          = "ol_csrf_token";

    @Override
    public SecurityContext loadContext( HttpRequestResponseHolder requestResponseHolder ) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String token = tokenFromRequest( requestResponseHolder.getRequest() );
        Authentication authentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken( token );
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

        final String authorizationInfo;
        final String authorizationHeader = getAuthorizationTokenFromHeader( request );

        if ( authorizationHeader != null ) {
            authorizationInfo = authorizationHeader;
        } else {
            authorizationInfo = getRequestCookie( request, AUTHORIZATION_COOKIE );
            final String csrfTokenFromCookie = getRequestCookie( request, CSRF_COOKIE );
            final String csrfTokenFromParams = request.getParameter( CSRF_COOKIE );
            if ( csrfTokenFromCookie == null || csrfTokenFromParams == null ) {
                return null;
            }
            if ( !StringUtils.equals( csrfTokenFromCookie, csrfTokenFromParams ) ) {
                return null;
            }
        }

        if ( authorizationInfo == null || !authorizationInfo.startsWith( BEARER_PREFIX ) ) {
            return null;
        }

        //Since " " is a single character string it is optimized to not use a pattern.
        final String[] parts = authorizationInfo.split( " " );

        if ( parts.length != 2 ) {
            // "Unauthorized: Format is Authorization: Bearer [token]"
            return null;
        }

        return parts[ 1 ];
    }

    private static String getRequestCookie( HttpServletRequest request, String targetCookie ) {
        Cookie[] cookies = request.getCookies();
        if ( cookies != null ) {
            for ( Cookie cookie : request.getCookies() ) {
                if ( StringUtils.equals( cookie.getName(), targetCookie ) ) {
                    try {
                        return URLDecoder.decode( cookie.getValue(), StandardCharsets.UTF_8.name() );
                    } catch ( UnsupportedEncodingException e ) {
                        logger.error( "Unable to decode {} cookie.", targetCookie );
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
