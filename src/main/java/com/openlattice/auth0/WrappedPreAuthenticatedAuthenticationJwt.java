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

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import java.util.Collection;
import javax.security.auth.Subject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class WrappedPreAuthenticatedAuthenticationJwt implements Authentication {
    private final PreAuthenticatedAuthenticationJsonWebToken token;

    public WrappedPreAuthenticatedAuthenticationJwt( PreAuthenticatedAuthenticationJsonWebToken token ) {
        this.token = token;
    }

    public PreAuthenticatedAuthenticationJsonWebToken unwrap() {
        return token;
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof WrappedPreAuthenticatedAuthenticationJwt ) ) { return false; }

        WrappedPreAuthenticatedAuthenticationJwt that = (WrappedPreAuthenticatedAuthenticationJwt) o;

        return token.equals( that.token );
    }

    @Override public int hashCode() {
        return token.hashCode();
    }

    @Override public String toString() {
        return "WrappedPreAuthenticatedAuthenticationJwt{" +
                "token=" + token +
                '}';
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return token.getAuthorities();
    }

    @Override public Object getCredentials() {
        return token.getCredentials();
    }

    @Override public Object getDetails() {
        return token.getDetails();
    }

    @Override public Object getPrincipal() {
        return token.getPrincipal();
    }

    @Override public boolean isAuthenticated() {
        return token.isAuthenticated();
    }

    @Override public void setAuthenticated( boolean isAuthenticated ) throws IllegalArgumentException {
        token.setAuthenticated( isAuthenticated );
    }

    @Override public String getName() {
        return token.getName();
    }

    public static PreAuthenticatedAuthenticationJsonWebToken usingToken( String token ) {
        return PreAuthenticatedAuthenticationJsonWebToken.usingToken( token );
    }

    public String getToken() {
        return token.getToken();
    }

    public String getKeyId() {
        return token.getKeyId();
    }

    public Authentication verify( JWTVerifier verifier ) throws JWTVerificationException {
        return token.verify( verifier );
    }

    @Override public boolean implies( Subject subject ) {
        return token.implies( subject );
    }
}
