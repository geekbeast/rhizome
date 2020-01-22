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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AwsAuth0TokenProvider implements Auth0TokenProvider {
    private static final int RETRY_MILLIS = 30000;

    private final AuthAPI          auth0Api;
    private final String           managementApiUrl;
    private final Supplier<String> token;
    private final Lock             tokenLock = new ReentrantLock();


    AwsAuth0TokenProvider( AuthAPI auth0Api, Auth0Configuration auth0Configuration ) {
        this.auth0Api = auth0Api;
        this.managementApiUrl = auth0Configuration.getManagementApiUrl();

        token = () -> {
            try {
                final var tokenHolder = auth0Api.requestToken( managementApiUrl ).execute();;
                long expiresInMillis = ( tokenHolder.getExpiresIn() * 1000 ) / 2;
                tokenLock.lock();
                token = Suppliers.memoizeWithExpiration( this, expiresInMillis, TimeUnit.MILLISECONDS );
                return tokenHolder.getAccessToken();

            } catch ( Auth0Exception e ) {
                token = Suppliers.memoizeWithExpiration( token, RETRY_MILLIS, TimeUnit.MILLISECONDS );
                return "";
            }
        };
    }

    public AwsAuth0TokenProvider( Auth0Configuration auth0Configuration ) {
        this( new AuthAPI(
                auth0Configuration.getDomain(),
                auth0Configuration.getClientId(),
                auth0Configuration.getClientSecret()
        ), auth0Configuration );
    }

    private functio

    @Override public String getManagementApiUrl() {
        return managementApiUrl;
    }

    @Override public String getToken() {
        return token.get();
    }
}
