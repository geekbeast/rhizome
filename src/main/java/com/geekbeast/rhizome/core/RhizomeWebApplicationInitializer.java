package com.geekbeast.rhizome.core;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.eclipse.jetty.servlets.GzipFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.containers.GzipConfiguration;
import com.geekbeast.rhizome.configuration.containers.JettyConfiguration;
import com.geekbeast.rhizome.pods.SpringDispatcherServletPod;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hazelcast.web.SessionListener;
import com.hazelcast.web.WebFilter;

public class RhizomeWebApplicationInitializer implements WebApplicationInitializer {
    private static final String HAZELCAST_SESSION_FILTER_NAME = "hazelcastSessionFilter";
    private static final String GZIP_FILTER_NAME = "GzipFilter";
    private static final String CONTEXT_CONFIG_LOCATION_PARAMETER_NAME = "contextConfigLocation";
    private static final String CONTEXT_PARAMETER_NAME = "contextClass";
    private static final String MIME_TYPES_PARAM = "mimeTypes";
    protected static final AnnotationConfigWebApplicationContext rhizomeContext = new AnnotationConfigWebApplicationContext();
    
    
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.addListener( new SessionListener() );
        
        /*
         * We have the luxury of being able to access the RhizomeConfiguration from the rhizomeContext.
         * This allows us to conditionally enabled session clustering among other things.
         */
        JettyConfiguration jettyConfig   = rhizomeContext.getBean( JettyConfiguration.class );
        RhizomeConfiguration configuration = rhizomeContext.getBean( RhizomeConfiguration.class );
        if( configuration.isSessionClusteringEnabled() ) {
            servletContext
            .addFilter( HAZELCAST_SESSION_FILTER_NAME , rhizomeContext.getBean( WebFilter.class ) )
            .addMappingForUrlPatterns(
                    Sets.newEnumSet( ImmutableSet.of( DispatcherType.FORWARD  , DispatcherType.REQUEST , DispatcherType.REQUEST ) , DispatcherType.class ) , 
                    false , 
                    "/*");
        }
        
        Optional<GzipConfiguration> gzipConfig = jettyConfig.getGzipConfiguration();
        if( gzipConfig.isPresent() && gzipConfig.get().isGzipEnabled() ) {
            FilterRegistration.Dynamic gzipFilter = servletContext.addFilter( GZIP_FILTER_NAME , new GzipFilter() );
            gzipFilter.addMappingForUrlPatterns( null, false, "/*");
            gzipFilter.setInitParameter( MIME_TYPES_PARAM , Joiner.on(",").skipNulls().join( gzipConfig.get().getGzipContentTypes() )  );
        }
        
        //Prevent jersey-spring3 from trying to initialize a spring application context.
        servletContext.setInitParameter( CONTEXT_CONFIG_LOCATION_PARAMETER_NAME , "" );
        servletContext.addListener( new ContextLoaderListener( rhizomeContext ) );
        servletContext.addListener( new RequestContextListener() );
        
        /* 
         * Spring MVC Servlet
         */
        
        AnnotationConfigWebApplicationContext metricServletContext = new AnnotationConfigWebApplicationContext();
        metricServletContext.setParent( rhizomeContext );
        metricServletContext.register( SpringDispatcherServletPod.class );
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet( "metricServlet" , new DispatcherServlet( metricServletContext ) );
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping( "/metrics/*" );
        
        /*
         * Jersey Servlet
         * For lovers of the JAX-RS standard.  
         */
        
        ServletRegistration.Dynamic jerseyDispatcher = 
            servletContext.addServlet( "jerseyServlet" , new ServletContainer( ) );
        jerseyDispatcher.setInitParameter( "javax.ws.rs.Application", RhizomeApplication.class.getName() );
        jerseyDispatcher.setLoadOnStartup(1);
        jerseyDispatcher.addMapping("/health*");
        
        /*
         * Atmosphere Servlet  
         */
        
    }
    
    public static AnnotationConfigWebApplicationContext getContext() { 
        return rhizomeContext;
    }
    
}
