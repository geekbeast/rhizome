package digital.loom.rhizome.authentication;

import java.io.IOException;

import javax.inject.Inject;

import com.openlattice.ResourceConfigurationLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.services.s3.AmazonS3;
import com.auth0.Auth0;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;

import digital.loom.rhizome.configuration.auth0.Auth0Configuration;

@Configuration
@Profile( Profiles.AWS_CONFIGURATION_PROFILE )
public class AwsAuth0Pod {
    @Inject
    private AmazonLaunchConfiguration awsConfig;

    @Inject
    private AmazonS3                  s3;

    @Bean
    public Auth0Configuration auth0Configuration() {
        return ResourceConfigurationLoader.loadConfigurationFromS3( s3,
                awsConfig.getBucket(),
                awsConfig.getFolder(),
                Auth0Configuration.class );
    }

    @Bean
    public Auth0 auth0() throws IOException {
        return new Auth0( auth0Configuration().getClientId(), auth0Configuration().getDomain() );
    }
}
