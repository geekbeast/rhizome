package digital.loom.rhizome.authentication;

import com.openlattice.authentication.AuthenticationTest;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import digital.loom.rhizome.configuration.auth0.Auth0Configuration;

@Configuration
public class Auth0TestPod extends LocalAuth0Pod {
    @Bean
    @Override
    public Auth0Configuration auth0Configuration() throws IOException {
        return super.auth0Configuration();
    }
}
