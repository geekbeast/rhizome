package digital.loom.rhizome.authentication;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import digital.loom.rhizome.configuration.auth0.Auth0Configuration;

@Configuration
public class Auth0TestPod extends Auth0Pod {
    @Bean
    @Override
    public Auth0Configuration auth0Configuration() throws IOException {
        this.configService.setConfiguration( AuthenticationTest.configuration );
        return super.auth0Configuration();
    }
}
