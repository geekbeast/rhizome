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

import com.amazonaws.services.s3.AmazonS3;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.openlattice.ResourceConfigurationLoader;
import com.openlattice.authentication.Auth0Configuration;
import javax.inject.Inject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile( { Profiles.AWS_CONFIGURATION_PROFILE, Profiles.AWS_TESTING_PROFILE } )
public class AwsAuth0Pod {
    @Inject
    private AmazonLaunchConfiguration awsConfig;

    @Inject
    private AmazonS3 s3;

    @Bean
    public Auth0Configuration auth0Configuration() {
        return ResourceConfigurationLoader.loadConfigurationFromS3( s3,
                awsConfig.getBucket(),
                awsConfig.getFolder(),
                Auth0Configuration.class );
    }
}
