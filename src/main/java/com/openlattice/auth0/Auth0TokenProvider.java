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
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.openlattice.authentication.Auth0Configuration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth0TokenProvider {
    private static final int    RETRY_MILLIS = 30000;
    private static final Logger logger       = LoggerFactory.getLogger( Auth0TokenProvider.class );

    private final AuthAPI          auth0Api;
    private final String           managementApiUrl;
    private       Supplier<String> token;

    public Auth0TokenProvider( Auth0Configuration auth0Configuration ) {
        this.auth0Api = new AuthAPI(
                auth0Configuration.getDomain(),
                auth0Configuration.getClientId(),
                auth0Configuration.getClientSecret()
        );

        this.managementApiUrl = auth0Configuration.getManagementApiUrl();

        token = () ->
        {
            try {
                TokenHolder holder = auth0Api.requestToken( managementApiUrl ).execute();
                long expiresInMillis = ( holder.getExpiresIn() * 1000 ) / 2;
                token = Suppliers.memoizeWithExpiration( this.token, expiresInMillis, TimeUnit.MILLISECONDS );
                return holder.getAccessToken();
            } catch ( Auth0Exception e ) {
                token = Suppliers.memoizeWithExpiration( this.token, RETRY_MILLIS, TimeUnit.MILLISECONDS );
                return "";
            }
        };
    }

    public String getManagementApiUrl() {
        return managementApiUrl;
    }

    public String getToken() {
        return token.get();
    }
}
