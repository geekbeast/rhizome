package digital.loom.rhizome.authentication;

import com.auth0.spring.security.api.Auth0CORSFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConfigurableAuth0CORSFilter extends Auth0CORSFilter {

    private HttpHeaders httpResponseHeaders;

    public ConfigurableAuth0CORSFilter( HttpHeaders headers ) {

        this.httpResponseHeaders = headers;
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
            throws IOException, ServletException {

        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        httpResponseHeaders.forEach( ( headerName, headerValue ) -> {

            if ( headerValue.size() == 1 ) {
                response.setHeader( headerName, headerValue.get( 0 ) );
            } else {
                response.setHeader( headerName, StringUtils.joinWith( ",", headerValue ) );
            }
        } );

        filterChain.doFilter( servletRequest, servletResponse );
    }

}
