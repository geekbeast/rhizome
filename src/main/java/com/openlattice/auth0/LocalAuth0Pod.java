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

import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.openlattice.ResourceConfigurationLoader;
import com.openlattice.authentication.Auth0Configuration;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile( Profiles.LOCAL_CONFIGURATION_PROFILE )
public class LocalAuth0Pod {
    @Bean
    public Auth0Configuration auth0Configuration() {
        return ResourceConfigurationLoader.loadConfiguration( Auth0Configuration.class );
    }
}

