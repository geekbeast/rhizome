package com.geekbeast.rhizome.core;

import static com.google.common.base.Preconditions.checkState;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.geekbeast.rhizome.configuration.ConfigurationConstants.Profiles;
import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.configuration.servlets.DispatcherServletConfiguration;
import com.geekbeast.rhizome.pods.AsyncPod;
import com.geekbeast.rhizome.pods.ConfigurationPod;
import com.geekbeast.rhizome.pods.JettyContainerPod;
import com.geekbeast.rhizome.pods.LoamPod;
import com.geekbeast.rhizome.pods.MetricsPod;
import com.geekbeast.rhizome.services.ServiceState;
import com.geekbeast.rhizome.startup.Requirement;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.hazelcast.web.SessionListener;
import com.hazelcast.web.WebFilter;
import com.geekbeast.rhizome.configuration.configuration.amazon.AmazonLaunchConfiguration;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Note: if using jetty, jetty creates an instance of this class with a no-arg constructor in order to call onStartup
 * TODO: break out WebApplicationInitializer's onStartup to a different class because of Jetty issue
 */
public class Rhizome implements WebApplicationInitializer {
    protected static final Class<?>[]                            REQUIRED_RHIZOME_PODS         = new Class<?>[] {
            ConfigurationPod.class,
            MetricsPod.class,
            AsyncPod.class };
    protected static final Lock                                  startupLock                   = new ReentrantLock();
    private static final   Logger                                logger                        = LoggerFactory
            .getLogger( Rhizome.class );
    private static final   String                                HAZELCAST_SESSION_FILTER_NAME = "hazelcastSessionFilter";
    protected static       AnnotationConfigWebApplicationContext rhizomeContext                = null;

    protected final AnnotationConfigWebApplicationContext context;
    private         JettyLoam                             jetty;
    private         EventBus                              eventBus;

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
        if ( loamPodClass != null ) {
            intercrop( loamPodClass );
        }
        intercrop( getDefaultServicePods() );
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

        /*
         * Setup hazelcast to provide an IMap to back session storage.
         */
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
         * Setup metrics admin servlet
         */

        ServletRegistration.Dynamic adminServlet = servletContext.addServlet( "admin", AdminServlet.class );
        adminServlet.setLoadOnStartup( 1 );
        adminServlet.addMapping( "/admin/*" );
        adminServlet.setInitParameter( "show-jvm-metrics", "true" );

        /*
         * Setup prometheus servlet
         */

        ServletRegistration.Dynamic prometheusServlet = servletContext.addServlet(
                "prometheus",
                new io.prometheus.client.exporter.MetricsServlet( CollectorRegistry.defaultRegistry )
        );
        prometheusServlet.setLoadOnStartup( 1 );
        prometheusServlet.addMapping( "/prometheus/*" );

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
            logger.info( " Registering dispatcher servlet: {}", configuration );
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

    public void sprout( String... activeProfiles ) {
        boolean awsProfile = shoot( context, activeProfiles );

        /*
         * This will trigger creation of Jetty, so we:
         * 1) Lock on singleton context
         * 2) switch in the correct singleton context
         * 3) set back to null and release lock once Jetty has finished starting.
         */
        try {
            startupLock.lock();
            checkState(
                    rhizomeContext == null,
                    "Rhizome context should be null before startup of startup." );
            rhizomeContext = context;
            context.refresh();
            eventBus = rhizomeContext.getBean( EventBus.class );
            startJetty( awsProfile );
        } catch ( Exception e ) {
            logger.error( "Something went wrong during startup", e );
            System.exit( 1 );
        } finally {
            rhizomeContext = null;
            showBannerIfStartedOrExit( jetty, context );
            startupLock.unlock();
            eventBus.post( ServiceState.RUNNING );
        }
    }

    protected void startJetty( boolean awsProfile ) throws Exception {
        JettyConfiguration jettyConfig = Preconditions.checkNotNull( rhizomeContext.getBean( JettyConfiguration.class ),
                "Jetty configuration cannot be null" );
        if ( awsProfile ) {
            this.jetty = new AwsJettyLoam( jettyConfig,
                    Preconditions.checkNotNull( rhizomeContext.getBean( AmazonLaunchConfiguration.class ),
                            "AwsConfig cannot be null" ) );
        } else {
            // For now we assume just AWS as an alternate to a local profile.
            this.jetty = new JettyLoam( jettyConfig );
        }

        eventBus.post( ServiceState.JETTY_STARTING );

        jetty.start();

        eventBus.post( ServiceState.JETTY_STARTED );
    }

    public void wilt() throws Exception {
        jetty.stop();
        context.close();
    }

    public Class<?>[] getDefaultServicePods() {
        return REQUIRED_RHIZOME_PODS;
    }

    public static boolean shoot( AbstractApplicationContext context, String... activeProfiles ) {
        boolean awsProfile = false;
        boolean localProfile = false;
        for ( String profile : activeProfiles ) {
            if ( StringUtils.equals( Profiles.AWS_CONFIGURATION_PROFILE, profile )
                    || StringUtils.equals( Profiles.AWS_TESTING_PROFILE, profile ) ) {
                awsProfile = true;
                logger.info( "Using AWS profile for configuration." );
            }

            if ( StringUtils.equals( Profiles.LOCAL_CONFIGURATION_PROFILE, profile ) ) {
                localProfile = true;
                logger.info( "Using Local profile for configuration." );
            }

            context.getEnvironment().addActiveProfile( profile );
        }

        if ( !awsProfile && !localProfile ) {
            context.getEnvironment().addActiveProfile( Profiles.LOCAL_CONFIGURATION_PROFILE );
        }
        return awsProfile;
    }

    static void showBannerIfStartedOrExit( JettyLoam jetty, AbstractApplicationContext context ) {
        checkState( jetty.getServer().isStarted(), "Jetty server is not started." );
        showBannerIfStartedOrExit( context );
    }

    static void showBannerIfStartedOrExit( AbstractApplicationContext context ) {
        checkState( context.isRunning(), "Application context is not running." );
        checkState( context.isActive(), "Application context is not active." );
        checkState( startupRequirementsSatisfied( context ), "Startup requirements have not been met." );

        showBanner();
    }

    public static void showBanner() {
        try {
            logger.info( "\n\n{}\n\n", Resources.toString( Resources.getResource( "banner.txt" ), Charsets.UTF_8 ) );
        } catch ( IOException e ) {
            logger.warn( "No startup banner found." );
        }
    }

    public static boolean startupRequirementsSatisfied( AbstractApplicationContext context ) {
        return context.getBeansOfType( Requirement.class )
                .values()
                .parallelStream()
                .allMatch( Requirement::isSatisfied );
    }
}
