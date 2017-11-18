package digital.loom.rhizome.authentication;

import com.auth0.Auth0;
import com.kryptnostic.rhizome.configuration.ConfigurationConstants.Profiles;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;
import com.openlattice.ResourceConfigurationLoader;
import digital.loom.rhizome.configuration.auth0.Auth0Configuration;
import java.io.IOException;
import javax.inject.Inject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile( Profiles.LOCAL_CONFIGURATION_PROFILE )
public class LocalAuth0Pod {
    @Bean
    public Auth0Configuration auth0Configuration() throws IOException {
        return ResourceConfigurationLoader.loadConfiguration( Auth0Configuration.class );
    }

    @Bean
    public Auth0 auth0() throws IOException {
        return new Auth0( auth0Configuration().getClientId(), auth0Configuration().getDomain() );
    }
}

