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

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import com.openlattice.authentication.Auth0Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

public class Auth0TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger( Auth0TokenProvider.class );

    private static final int CHECK_EXP_INTERVAL_MILLIS = 60 * 60 * 1000; // 1 hour

    private Auth0Configuration auth0Configuration;
    private AuthAPI            auth0Api;
    private String             token;
    private AtomicLong         tokenExp;

    public Auth0TokenProvider( Auth0Configuration auth0Configuration ) {
        this.auth0Configuration = auth0Configuration;
        this.auth0Api = new AuthAPI(
                auth0Configuration.getDomain(),
                auth0Configuration.getClientId(),
                auth0Configuration.getClientSecret()
        );
        this.tokenExp = new AtomicLong( System.currentTimeMillis() );
    }

    public String getToken() {
        if ( StringUtils.isBlank( token ) || tokenExp.get() < System.currentTimeMillis() ) {
            renewAuth0Token();
        }
        return token;
    }

    @Scheduled( fixedRate = CHECK_EXP_INTERVAL_MILLIS )
    private void renewAuth0Token() {
        if ( tokenExp.get() < System.currentTimeMillis() ) {
            logger.info( "Attempting to renew Auth0 Management APIv2 token." );
            try {
                AuthRequest request = auth0Api.requestToken( auth0Configuration.getManagementApiUrl() );
                TokenHolder holder = request.execute();
                this.token = holder.getAccessToken();

                // set expiration to be half of the real expiration
                long expiresInMillis = ( holder.getExpiresIn() * 1000 ) / 2;
                this.tokenExp.set( System.currentTimeMillis() + expiresInMillis );
                logger.info( "Successfully renewed Auth0 Management APIv2 token." );
            } catch ( Auth0Exception e ) {
                logger.error( "Failed to renew Auth0 Management APIv2 token.", e );
            }
        }
    }
}
