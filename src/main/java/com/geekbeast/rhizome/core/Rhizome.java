package com.geekbeast.rhizome.core;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlets.GzipFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.BeansException;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.jetty.GzipConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.configuration.servlets.DispatcherServletConfiguration;
import com.geekbeast.rhizome.pods.AsyncPod;
import com.geekbeast.rhizome.pods.ConfigurationPod;
import com.geekbeast.rhizome.pods.HazelcastPod;
import com.geekbeast.rhizome.pods.MetricsPod;
import com.geekbeast.rhizome.pods.ServletContainerPod;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hazelcast.web.SessionListener;
import com.hazelcast.web.WebFilter;

/**
 * Note: if using jetty, jetty creates an instance of this class with a no-arg constructor in order to call onStartup
 * TODO: break out WebApplicationInitializer's onStartup to a different class because of Jetty issue
 *
 */
public class Rhizome implements WebApplicationInitializer {
    private static final String                                  HAZELCAST_SESSION_FILTER_NAME = "hazelcastSessionFilter";
    private static final String                                  GZIP_FILTER_NAME              = "GzipFilter";
    private static final String                                  MIME_TYPES_PARAM              = "mimeTypes";
    protected final static AnnotationConfigWebApplicationContext rhizomeContext                = new AnnotationConfigWebApplicationContext();
    protected static boolean                                     isInitialized                 = false;

    public Rhizome() {
        this( new Class[ 0 ] );
    }

    public Rhizome( Class<?>... pods ) {
        intercrop( pods );
        initialize();
    }

    @Override
    public void onStartup( ServletContext servletContext ) throws ServletException {
        servletContext.addListener( new SessionListener() );

        /*
         * We have the luxury of being able to access the RhizomeConfiguration from the rhizomeContext. This allows us
         * to conditionally enabled session clustering among other things.
         */

        JettyConfiguration jettyConfig = rhizomeContext.getBean( JettyConfiguration.class );
        RhizomeConfiguration configuration = rhizomeContext.getBean( RhizomeConfiguration.class );
        if ( configuration.isSessionClusteringEnabled() ) {
            servletContext.addFilter( HAZELCAST_SESSION_FILTER_NAME, rhizomeContext.getBean( WebFilter.class ) )
                    .addMappingForUrlPatterns(
                            Sets.newEnumSet( ImmutableSet.of(
                                    DispatcherType.FORWARD,
                                    DispatcherType.REQUEST,
                                    DispatcherType.REQUEST ), DispatcherType.class ),
                            false,
                            "/*" );
        }

        Optional<GzipConfiguration> gzipConfig = jettyConfig.getGzipConfiguration();
        if ( gzipConfig.isPresent() && gzipConfig.get().isGzipEnabled() ) {
            FilterRegistration.Dynamic gzipFilter = servletContext.addFilter( GZIP_FILTER_NAME, new GzipFilter() );
            gzipFilter.addMappingForUrlPatterns( null, false, "/*" );
            gzipFilter.setInitParameter(
                    MIME_TYPES_PARAM,
                    Joiner.on( "," ).skipNulls().join( gzipConfig.get().getGzipContentTypes() ) );
        }

        // Prevent jersey-spring3 from trying to initialize a spring application context.
        // servletContext.setInitParameter( CONTEXT_CONFIG_LOCATION_PARAMETER_NAME , "" );
        servletContext.addListener( new ContextLoaderListener( rhizomeContext ) );
        servletContext.addListener( new RequestContextListener() );

        // Register the health check registry.
        servletContext.setAttribute(
                HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                rhizomeContext.getBean( "healthCheckRegistry", HealthCheckRegistry.class ) );
        servletContext.setAttribute(
                MetricsServlet.METRICS_REGISTRY,
                rhizomeContext.getBean( "serverMetricRegistry", MetricRegistry.class ) );

        /*
         * 
         */

        ServletRegistration.Dynamic adminServlet = servletContext.addServlet( "admin", AdminServlet.class );
        adminServlet.setLoadOnStartup( 1 );
        adminServlet.addMapping( "/admin/*" );
        adminServlet.setInitParameter( "show-jvm-metrics", "true" );

        /*
         * Jersey Servlet For lovers of the JAX-RS standard.
         */

        ServletRegistration.Dynamic jerseyDispatcher = servletContext.addServlet(
                "defaultJerseyServlet",
                new ServletContainer() );
        jerseyDispatcher.setInitParameter( "javax.ws.rs.Application", RhizomeApplication.class.getName() );
        jerseyDispatcher.setLoadOnStartup( 1 );
        jerseyDispatcher.addMapping( "/health/*" );

        /*
         * Atmosphere Servlet
         */

        // TODO: Add support for atmosphere servlet.

        /*
         * Default Servlet
         */
        ServletRegistration.Dynamic defaultServlet = servletContext.addServlet( "default", new DefaultServlet() );
        defaultServlet.addMapping( new String[] { "/*" } );
        defaultServlet.setLoadOnStartup( 1 );

        registerDispatcherServlets( servletContext );
    }

    private void registerDispatcherServlets( ServletContext servletContext ) {
        Map<String, DispatcherServletConfiguration> dispatcherServletsConfigs = rhizomeContext.getBeansOfType(
                DispatcherServletConfiguration.class,
                false,
                true );
        for ( Entry<String, DispatcherServletConfiguration> configPair : dispatcherServletsConfigs.entrySet() ) {
            DispatcherServletConfiguration configuration = configPair.getValue();
            AnnotationConfigWebApplicationContext dispatchServletContext = new AnnotationConfigWebApplicationContext();
            dispatchServletContext.setParent( rhizomeContext );
            dispatchServletContext.register( configuration.getPods().toArray( new Class<?>[ 0 ] ) );
            ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
                    configuration.getServletName(),
                    new DispatcherServlet( dispatchServletContext ) );
            if ( configuration.getLoadOnStartup().isPresent() ) {
                dispatcher.setLoadOnStartup( configuration.getLoadOnStartup().get() );
            }
            dispatcher.addMapping( configuration.getMappings() );
        }
    }

    public AnnotationConfigWebApplicationContext getContext() {
        return rhizomeContext;
    }

    public <T> T harvest( Class<T> clazz ) {
        return rhizomeContext.getBean( clazz );
    }

    public void intercrop( Class<?>... pods ) {
        if ( pods != null && pods.length > 0 ) {
            rhizomeContext.register( pods );
        }
    }

    public void sprout( String... activeProfiles ) throws Exception {
        for ( String profile : activeProfiles ) {
            rhizomeContext.getEnvironment().addActiveProfile( profile );
        }
        rhizomeContext.refresh();
        for ( Loam loam : rhizomeContext.getBeansOfType( Loam.class ).values() ) {
            loam.start();
        }
    }

    public void wilt() throws BeansException, Exception {
        for ( Loam loam : rhizomeContext.getBeansOfType( Loam.class ).values() ) {
            loam.stop();
        }
    }

    /**
     * This method should be overridden if any of the built-in defaults are not desired. To add additional
     * configurations beyond the built in defaults, {@code intercrop(...)} should be called to register @Configuration
     * bootstrap beans.
     */
    protected void initialize() {
        synchronized ( rhizomeContext ) {
            if ( !isInitialized ) {
                rhizomeContext.register( ConfigurationPod.class );
                rhizomeContext.register( MetricsPod.class );
                rhizomeContext.register( AsyncPod.class );
                rhizomeContext.register( HazelcastPod.class );
                rhizomeContext.register( ServletContainerPod.class );
                isInitialized = true;
            }
        }
    }
}
