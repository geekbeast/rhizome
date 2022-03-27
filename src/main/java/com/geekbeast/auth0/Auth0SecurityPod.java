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
import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import com.geekbeast.rhizome.core.RhizomeSecurity;
import com.geekbeast.authentication.Auth0AuthenticationConfiguration;
import com.geekbeast.authentication.Auth0Configuration;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.inject.Inject;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Including this pod will enable using Auth0 as an authentication source. It based off the one provided by auth0 in the
 * spring-mvc pod, but differs in that it enables {@code @Secured} annotations, disables debug, and allows configuration
 * using properties file.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        prePostEnabled = true )
@EnableWebSecurity(
        debug = false )
public class Auth0SecurityPod extends WebSecurityConfigurerAdapter {
    @Inject
    private Auth0Configuration configuration;

    @Inject
    private AuthAPI auth0;

    @Override
    public void configure( WebSecurity web ) throws Exception {
        web.ignoring().antMatchers( HttpMethod.OPTIONS, "/**" );
    }

    @Override
    protected void configure( final HttpSecurity http ) throws Exception {
        for ( Auth0AuthenticationConfiguration config : configuration.getClients() ) {
            configure( http, config );
        }

        http.securityContext().securityContextRepository( new CookieOrBearerSecurityContextRepository() );

        // Apply the Authentication and Authorization Strategies your application endpoints require
        authorizeRequests( http );
    }

    /**
     * Lightweight default configuration that offers basic authorization checks for authenticated users on secured
     * endpoint, and sets up a Principal user object with granted authorities
     * <p>
     * For simple apps, this is sufficient, however for applications wishing to specify fine-grained endpoint access
     * restrictions, use Role / Group level endpoint authorization etc, then this configuration should be disabled and a
     * copy, augmented with your own requirements provided. See Sample app for example
     *
     * Override this function in subclass to apply custom authentication / authorization strategies to your application
     * endpoints
     */
    protected void authorizeRequests( HttpSecurity http ) throws Exception {
        //        http.authorizeRequests()
        //                .antMatchers( "/**" ).authenticated();
    }

    protected AuthAPI getAuthenticationApiClient() {
        return auth0;
    }

    private void configure( final HttpSecurity http, Auth0AuthenticationConfiguration configuration ) throws Exception {
        switch ( configuration.getSigningAlgorithm() ) {
            case "HS256":
                final byte[] secret;
                if ( configuration.isBase64EncodedSecret() ) {
                    secret = Base64.getUrlDecoder().decode( configuration.getSecret() );
                } else {
                    secret = configuration.getSecret().getBytes( StandardCharsets.UTF_8 );
                }
                JwtWebSecurityConfigurer
                        .forHS256( configuration.getAudience(), configuration.getIssuer(), secret )
                        .configure( http );
                break;
            case "RS256":
                JwtWebSecurityConfigurer
                        .forRS256( configuration.getAudience(), configuration.getIssuer() )
                        .configure( http );
                break;
            default:
                throw new IllegalArgumentException(
                        configuration.getSigningAlgorithm() + " is not a supported signing algorithm" );
        }
    }

}
