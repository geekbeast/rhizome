package digital.loom.rhizome.authentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 * Convenience class for automatically selecting the correct auth0 pod based on environment.
 */
@Configuration
@Import( { LocalAuth0Pod.class, AwsAuth0Pod.class } )
public class Auth0Pod {}
