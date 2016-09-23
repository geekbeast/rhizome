package digital.loom.rhizome.authentication;

import javax.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.auth0.spring.security.api.Auth0AuthenticationEntryPoint;
import com.auth0.spring.security.api.Auth0AuthenticationFilter;
import com.auth0.spring.security.api.Auth0AuthenticationProvider;
import com.auth0.spring.security.api.Auth0AuthorityStrategy;
import com.auth0.spring.security.api.Auth0CORSFilter;
import com.kryptnostic.rhizome.core.JettyAnnotationConfigurationHack;
import com.kryptnostic.rhizome.core.RhizomeSecurity;

import digital.loom.rhizome.configuration.auth0.Auth0Configuration;

/**
 * Including this pod will enable using Auth0 as an authentication source. It based off the one provided by auth0 in the
 * spring-mvc pod, but differs in that it enables {@code @Secured} annotations, disables debug, and allows configuration
 * using properties file.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    prePostEnabled = true )
@EnableWebSecurity(
    debug = false )
@Order( SecurityProperties.ACCESS_OVERRIDE_ORDER )
@ConditionalOnProperty(
    prefix = "auth0",
    name = "defaultAuth0ApiSecurityEnabled" )
public class Auth0SecurityPod extends WebSecurityConfigurerAdapter {

    @Inject
    private Auth0Configuration configuration;

    static {
        JettyAnnotationConfigurationHack.registerInitializer( RhizomeSecurity.class.getCanonicalName() );
    }

    @Bean(
        name = "auth0AuthenticationManager" )
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public Auth0CORSFilter simpleCORSFilter() {
        return new Auth0CORSFilter();
    }

    @Bean(
        name = "auth0AuthenticationProvider" )
    public Auth0AuthenticationProvider auth0AuthenticationProvider() {
        // First check the authority strategy configured for the API
        if ( !Auth0AuthorityStrategy.contains( configuration.getAuthorityStrategy() ) ) {
            throw new IllegalStateException( "Configuration error, illegal authority strategy" );
        }
        final Auth0AuthorityStrategy authorityStrategy = Auth0AuthorityStrategy
                .valueOf( configuration.getAuthorityStrategy() );
        final Auth0AuthenticationProvider authenticationProvider = new Auth0AuthenticationProvider();
        authenticationProvider.setDomain( configuration.getDomain() );
        authenticationProvider.setIssuer( configuration.getIssuer() );
        authenticationProvider.setClientId( configuration.getClientId() );
        authenticationProvider.setClientSecret( configuration.getClientSecret() );
        authenticationProvider.setSecuredRoute( configuration.getSecuredRoute() );
        authenticationProvider.setAuthorityStrategy( authorityStrategy );
        authenticationProvider.setBase64EncodedSecret( configuration.isBase64EncodedSecret() );

        return authenticationProvider;
    }

    @Bean(
        name = "auth0EntryPoint" )
    public Auth0AuthenticationEntryPoint auth0AuthenticationEntryPoint() {
        return new Auth0AuthenticationEntryPoint();
    }

    @Bean(
        name = "auth0Filter" )
    public Auth0AuthenticationFilter auth0AuthenticationFilter( final Auth0AuthenticationEntryPoint entryPoint ) {
        final Auth0AuthenticationFilter filter = new Auth0AuthenticationFilter();
        filter.setEntryPoint( entryPoint );
        return filter;
    }

    /**
     * We do this to ensure our Filter is only loaded once into Application Context
     *
     * If using Spring Boot, any GenericFilterBean in the context will be automatically added to the filter chain. Since
     * we want to support Servlet 2.x and 3.x we should not extend OncePerRequestFilter therefore instead we explicitly
     * define FilterRegistrationBean and disable.
     *
     */
    @Bean(
        name = "auth0AuthenticationFilterRegistration" )
    public FilterRegistrationBean auth0AuthenticationFilterRegistration( final Auth0AuthenticationFilter filter ) {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter( filter );
        filterRegistrationBean.setEnabled( false );
        return filterRegistrationBean;
    }

    @Override
    protected void configure( final AuthenticationManagerBuilder auth ) throws Exception {
        auth.authenticationProvider( auth0AuthenticationProvider() );
    }

    @Override
    public void configure( WebSecurity web ) throws Exception {
        web.ignoring().antMatchers( HttpMethod.OPTIONS, "/**" );
    }

    @Override
    protected void configure( final HttpSecurity http ) throws Exception {

        // Disable CSRF for JWT usage
        http.csrf().disable();

        // Add Auth0 Authentication Filter
        http.addFilterAfter( auth0AuthenticationFilter( auth0AuthenticationEntryPoint() ),
                SecurityContextPersistenceFilter.class )
                .addFilterBefore( simpleCORSFilter(), Auth0AuthenticationFilter.class );

        // Apply the Authentication and Authorization Strategies your application endpoints require
        authorizeRequests( http );

        // STATELESS - we want re-authentication of JWT token on every request
        http.sessionManagement().sessionCreationPolicy( SessionCreationPolicy.STATELESS );
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
        http.authorizeRequests()
                .antMatchers( "/**" ).authenticated();
    }

}
