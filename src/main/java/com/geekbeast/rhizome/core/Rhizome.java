package com.geekbeast.rhizome.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.eclipse.jetty.servlet.DefaultServlet;
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
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.configuration.servlets.DispatcherServletConfiguration;
import com.geekbeast.rhizome.pods.AsyncPod;
import com.geekbeast.rhizome.pods.ConfigurationPod;
import com.geekbeast.rhizome.pods.JettyContainerPod;
import com.geekbeast.rhizome.pods.LoamPod;
import com.geekbeast.rhizome.pods.MetricsPod;
import com.google.common.base.Preconditions;
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
    protected static final Class<?>[]                      DEFAULT_SERVICE_PODS          = new Class<?>[] {
                                                                                         ConfigurationPod.class,
                                                                                         MetricsPod.class,
                                                                                         AsyncPod.class };
    protected static final Lock                            startupLock                   = new ReentrantLock();
    protected static AnnotationConfigWebApplicationContext rhizomeContext                = null;
    protected final AnnotationConfigWebApplicationContext  context;

    public Rhizome() {
        this( new Class<?>[] {} );
    }

    public Rhizome( Class<?>... pods ) {
        this( JettyContainerPod.class, pods );
    }

    public Rhizome( Class<? extends LoamPod> loamPodClass, Class<?>... pods ) {
        this( new AnnotationConfigWebApplicationContext(), loamPodClass, pods );
    }

    public Rhizome(
            AnnotationConfigWebApplicationContext context,
            Class<? extends LoamPod> loamPodClass,
            Class<?>... pods ) {
        this.context = context;
        intercrop( pods );
        intercrop( loamPodClass );
        Arrays.asList( getDefaultServicePods() ).forEach( pod -> context.register( pod ) );
    }

    @Override
    public void onStartup( ServletContext servletContext ) throws ServletException {
        Preconditions.checkNotNull( rhizomeContext, "Rhizome context cannot be null for startup." );
        servletContext.addListener( new SessionListener() );

        /*
         * We have the luxury of being able to access the RhizomeConfiguration from the rhizomeContext. This allows us
         * to conditionally enabled session clustering among other things.
         */

        RhizomeConfiguration configuration = rhizomeContext.getBean( RhizomeConfiguration.class );
        JettyConfiguration jettyConfiguration = rhizomeContext.getBean( JettyConfiguration.class );

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
        if ( jettyConfiguration.isDefaultServletEnabled() ) {
            ServletRegistration.Dynamic defaultServlet = servletContext.addServlet( "default", new DefaultServlet() );
            defaultServlet.addMapping( new String[] { "/*" } );
            defaultServlet.setLoadOnStartup( 1 );
            defaultServlet.setAsyncSupported( true );
        }

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
            Preconditions.checkNotNull( dispatcher,
                    "A DispatcherServlet with this name has already been registered and fully configured" );
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
         * This will trigger creation of Jetty, so we: 1) Lock on singleton context 2) switch in the correct singleton
         * context 3) set back to null and release lock once Jetty has finished starting.
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
        } finally {
            rhizomeContext = null;
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

    // This method is not static so it can be sub-classed and overridden.

    public Class<?>[] getDefaultServicePods() {
        return DEFAULT_SERVICE_PODS;
    }
}
