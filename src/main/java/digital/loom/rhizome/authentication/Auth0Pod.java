package digital.loom.rhizome.authentication;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.auth0.Auth0;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

import digital.loom.rhizome.configuration.auth0.Auth0Configuration;

@Configuration
public class Auth0Pod {
    @Inject
    protected ConfigurationService configService;
    
    @Bean
    public Auth0Configuration auth0Configuration() throws IOException {
        return configService.getConfiguration( Auth0Configuration.class );
    }

    @Bean
    public Auth0 auth0() throws IOException {
        return new Auth0( auth0Configuration().getClientId(), auth0Configuration().getDomain() );
    }
}
