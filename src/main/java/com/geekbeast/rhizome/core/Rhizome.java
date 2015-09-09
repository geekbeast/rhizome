package com.geekbeast.rhizome.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import jersey.repackaged.com.google.common.base.Preconditions;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlets.GzipFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.BeansException;
import org.springframework.scheduling.annotation.Async;
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
import com.geekbeast.rhizome.pods.hazelcast.BaseHazelcastInstanceConfigurationPod;
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
    private static final String                            HAZELCAST_SESSION_FILTER_NAME = "hazelcastSessionFilter";
    private static final String                            GZIP_FILTER_NAME              = "GzipFilter";
    private static final String                            MIME_TYPES_PARAM              = "mimeTypes";
    protected static Lock                                  startupLock                   = new ReentrantLock();
    protected static AnnotationConfigWebApplicationContext rhizomeContext                = null;
    protected AtomicBoolean                         isInitialized                 = new AtomicBoolean( false );
    protected final AnnotationConfigWebApplicationContext  context;

    public Rhizome() {
        this( new Class[ 0 ] );
    }

    public Rhizome( Class<?>... pods ) {
        this( new AnnotationConfigWebApplicationContext(), pods );
    }

    public Rhizome( AnnotationConfigWebApplicationContext context, Class<?>... pods ) {
        this.context = context;
        intercrop( pods );
        initialize();
    }

    @Async
    @Override
    public void onStartup( ServletContext servletContext ) throws ServletException {
        Preconditions.checkNotNull( rhizomeContext, "Rhizome context cannot be null for startup." );
        servletContext.addListener( new SessionListener() );

        /*
         * We have the luxury of being able to access the RhizomeConfiguration from the rhizomeContext. This allows us
         * to conditionally enabled session clustering among other things.
         */

        JettyConfiguration jettyConfig = rhizomeContext.getBean( JettyConfiguration.class );
        RhizomeConfiguration configuration = rhizomeContext.getBean( RhizomeConfiguration.class );
        if ( configuration.isSessionClusteringEnabled() ) {
            FilterRegistration.Dynamic addFilter = servletContext.addFilter(
                    HAZELCAST_SESSION_FILTER_NAME,
                    rhizomeContext.getBean( WebFilter.class ) );
            addFilter.addMappingForUrlPatterns(
                    Sets.newEnumSet(
                            ImmutableSet.of( DispatcherType.FORWARD, DispatcherType.REQUEST, DispatcherType.REQUEST ),
                            DispatcherType.class ),
                    false,
                    "/*" );
            addFilter.setAsyncSupported( true );
        }

        Optional<GzipConfiguration> gzipConfig = jettyConfig.getGzipConfiguration();
        if ( gzipConfig.isPresent() && gzipConfig.get().isGzipEnabled() ) {
            FilterRegistration.Dynamic gzipFilter = servletContext.addFilter( GZIP_FILTER_NAME, new GzipFilter() );
            gzipFilter.setAsyncSupported( true );
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
                rhizomeContext.getBean( "getHealthCheckRegistry", HealthCheckRegistry.class ) );
        servletContext.setAttribute(
                MetricsServlet.METRICS_REGISTRY,
                rhizomeContext.getBean( "getMetricRegistry", MetricRegistry.class ) );

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
        defaultServlet.setAsyncSupported( true );

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
            dispatcher.setAsyncSupported( true );
            dispatcher.addMapping( configuration.getMappings() );
        }
    }

    public AnnotationConfigWebApplicationContext getContext() {
        return context;
    }

    public <T> T harvest( Class<T> clazz ) {
        return context.getBean( clazz );
    }

    public void intercrop( Class<?>... pods ) {
        if ( pods != null && pods.length > 0 ) {
            context.register( pods );
        }
    }

    public void sprout( String... activeProfiles ) throws Exception {
        for ( String profile : activeProfiles ) {
            context.getEnvironment().addActiveProfile( profile );
        }

        /*
         * This will trigger creation of Jetty, so we: 1) Lock on singleton context 2)
         */
        try {
            startupLock.lock();
            Preconditions.checkState(
                    rhizomeContext == null,
                    "Rhizome context should be null before startup of startup." );
            rhizomeContext = context;
            context.refresh();
            for ( Loam loam : rhizomeContext.getBeansOfType( Loam.class ).values() ) {
                loam.start();
            }
            rhizomeContext = null;
        } finally {
            startupLock.unlock();
        }
    }

    public void wilt() throws BeansException, Exception {
        Collection<Loam> loams = context.getBeansOfType( Loam.class ).values();
        for ( Loam loam : loams ) {
            loam.stop();
        }
        for ( Loam loam : loams ) {
            loam.join();
        }
    }

    /**
     * This method should be overridden if any of the built-in defaults are not desired. To add additional
     * configurations beyond the built in defaults, {@code intercrop(...)} should be called to register @Configuration
     * bootstrap beans.
     */
    protected void initialize() {
        synchronized ( context ) {
            if ( !isInitialized.getAndSet( true ) ) {
                Arrays.asList( getDefaultPods() ).forEach( pod -> context.register( pod ) );
            }
        }
    }

    public Class<?>[] getDefaultPods() {
        return new Class<?>[] { ConfigurationPod.class, MetricsPod.class, AsyncPod.class, HazelcastPod.class,
                ServletContainerPod.class, BaseHazelcastInstanceConfigurationPod.class };
    }
}
