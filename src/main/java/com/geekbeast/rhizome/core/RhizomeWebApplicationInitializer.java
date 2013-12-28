package com.geekbeast.rhizome.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.geekbeast.rhizome.pods.MetricsPod;
import com.geekbeast.rhizome.pods.SpringDispatcherServletPod;
import com.geekbeast.rhizome.pods.WebAppPod;

public class RhizomeWebApplicationInitializer implements WebApplicationInitializer {
    private static final String HAZELCAST_SESSION_FILTER_NAME = "hazelcastSessionFilter";
    private static final String CONTEXT_CONFIG_LOCATION_PARAMETER_NAME = "contextConfigLocation";
    private static final String CONTEXT_PARAMETER_NAME = "contextClass";
    
    protected static final AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    
    
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        rootContext.register( WebAppPod.class );
        rootContext.register( MetricsPod.class );
//        rootContext.register( JerseyResource.class );
//        rootContext.register( RhizomeApplication.class );
//        rootContext.refresh();
//        servletContext.addListener( new SessionListener() );
//        servletContext
//        .addFilter( HAZELCAST_SESSION_FILTER_NAME , (WebFilter) rootContext.getBean( WebFilter.class ) )
//        .addMappingForUrlPatterns(
//                Sets.newEnumSet( ImmutableSet.of( DispatcherType.FORWARD  , DispatcherType.REQUEST , DispatcherType.REQUEST ) , DispatcherType.class ) , 
//                false , 
//                "/*");
        
        //Prevent jersey-spring3 from trying to initialize a spring application context.
        servletContext.setInitParameter( CONTEXT_CONFIG_LOCATION_PARAMETER_NAME , "" );
        servletContext.addListener( new ContextLoaderListener( rootContext ) );
        servletContext.addListener( new RequestContextListener() );
        
        /* 
         * Spring MVC Servlet
         */
        
        AnnotationConfigWebApplicationContext metricServletContext = new AnnotationConfigWebApplicationContext();
        metricServletContext.setParent( rootContext );
        metricServletContext.register( SpringDispatcherServletPod.class );
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet( "metricServlet" , new DispatcherServlet( metricServletContext ) );
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping( "/metrics/*" );
        
        
        /*
         * Jersey Servlet
         */
        
        ServletRegistration.Dynamic jerseyDispatcher = 
            servletContext.addServlet( "jerseyServlet" , new ServletContainer( ) );
        jerseyDispatcher.setInitParameter( "javax.ws.rs.Application", RhizomeApplication.class.getName() );
        jerseyDispatcher.setLoadOnStartup(1);
        jerseyDispatcher.addMapping("/*");
        
        /*
         * Atmosphere Servlet  
         */
        
    }
    
}
