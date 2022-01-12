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
import com.kryptnostic.rhizome.pods.ConfigurationLoader;
import com.openlattice.authentication.Auth0Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlatice.com&gt; Convenience class for automatically selecting the correct
 *         auth0 pod based on environment.
 */
@Configuration
public class Auth0Pod {

    @Inject
    private ConfigurationLoader configurationLoader;

    @Bean
    public Auth0Configuration auth0Configuration() {
        return configurationLoader.load( Auth0Configuration.class );
    }

    @Bean
    public AuthAPI auth0() throws IOException {
        Auth0Configuration auth0Configuration = auth0Configuration();
        return new AuthAPI(
            auth0Configuration.getDomain(),
            auth0Configuration.getClientId(),
            auth0Configuration.getClientSecret()
        );
    }
}
