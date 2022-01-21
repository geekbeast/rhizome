package com.geekbeast.rhizome.tests.authentication;

import com.geekbeast.auth0.Auth0SecurityPod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableGlobalMethodSecurity(
        prePostEnabled = true )
@EnableWebSecurity(
        debug = false )
public class Auth0SecurityTestPod extends Auth0SecurityPod {
    @Override protected void configure( HttpSecurity http ) throws Exception {
        super.configure( http );
        http.authorizeRequests()
                .antMatchers( "/api/unsecured/**" ).authenticated()
                .antMatchers( "/api/secured/foo" ).hasAnyAuthority( "a", "b" )
                .antMatchers( "/api/secured/admin" ).hasAnyAuthority( "openid" )
                .antMatchers( "/api/secured/user" ).hasAnyAuthority( "email" );
    }
}
