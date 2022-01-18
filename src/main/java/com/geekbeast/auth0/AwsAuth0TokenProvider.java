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

package com.geekbeast.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.google.common.base.Supplier;
import com.geekbeast.authentication.Auth0Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Suppliers.memoizeWithExpiration;

public class AwsAuth0TokenProvider implements Auth0TokenProvider {
    private static final int RETRY_MILLIS = 30000;

    private final AuthAPI          auth0Api;
    private final String           managementApiUrl;
    private final Lock             tokenLock = new ReentrantLock();
    private       Supplier<String> token;

    AwsAuth0TokenProvider( AuthAPI auth0Api, Auth0Configuration auth0Configuration ) {
        this.auth0Api = auth0Api;
        this.managementApiUrl = auth0Configuration.getManagementApiUrl();

        final var th = getUpdatedTokenHolder();

        //This avoids having to make another call for token after initial constuction
        if( th != null ) {
            token = memoizeWithExpiration( th::getAccessToken,
                    getExpirationInMillis( th.getExpiresIn() ),
                    TimeUnit.MILLISECONDS );
        }
    }

    public AwsAuth0TokenProvider( Auth0Configuration auth0Configuration ) {
        this( new AuthAPI(
                auth0Configuration.getDomain(),
                auth0Configuration.getClientId(),
                auth0Configuration.getClientSecret()
        ), auth0Configuration );
    }

    private TokenHolder getUpdatedTokenHolder() {
        tokenLock.lock();
        try {
            final var tokenHolder = auth0Api.requestToken( managementApiUrl ).execute();
            token = memoizeWithExpiration( this::getUpdatedToken,
                    getExpirationInMillis( tokenHolder.getExpiresIn() ),
                    TimeUnit.SECONDS );
            return tokenHolder;
        } catch ( Auth0Exception e ) {
            token = memoizeWithExpiration( this::getUpdatedToken, RETRY_MILLIS, TimeUnit.SECONDS );
            return null;
        } finally {
            tokenLock.unlock();
        }
    }

    private String getUpdatedToken() {
        final var th = getUpdatedTokenHolder();
        if ( th == null ) {
            return "";
        }
        return th.getAccessToken();
    }

    @Override public String getManagementApiUrl() {
        return managementApiUrl;
    }

    @Override public String getToken() {
        try {
            tokenLock.lock();
            return token.get();
        } finally {
            tokenLock.unlock();
        }
    }

    private static long getExpirationInMillis( long expirationInSeconds ) {
        checkArgument( expirationInSeconds > 0 );
        return ( expirationInSeconds * 1000 * 4 ) / 5;
    }
}
