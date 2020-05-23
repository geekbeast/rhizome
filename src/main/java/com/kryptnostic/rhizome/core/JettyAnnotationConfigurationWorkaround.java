package com.kryptnostic.rhizome.core;

import com.google.common.collect.Lists;
import java.util.List;

import javax.servlet.ServletContainerInitializer;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class contains an exact replica of the annotation configuration class, with the exception that it registers a
 * {@code RhizomeWebApplicationInitializer} for discovery during class path scanning.
 * 
 * @author Matthew Tamayo-Rios
 */
public class JettyAnnotationConfigurationWorkaround extends AnnotationConfiguration {
    private static final Logger       LOG                    = Log.getLogger( JettyAnnotationConfigurationWorkaround.class );
    private static final List<String> additionalInitializers = Lists.newArrayList();

    static {
        additionalInitializers.add( Rhizome.class.getCanonicalName() );
    }

    @Override
    public void createServletContainerInitializerAnnotationHandlers(
            WebAppContext context,
            List<ServletContainerInitializer> scis ) {
        super.createServletContainerInitializerAnnotationHandlers( context, scis );
        final List<ContainerInitializer> initializers = (List<ContainerInitializer>) context.getAttribute( CONTAINER_INITIALIZERS );
        initializers.forEach( initializer -> additionalInitializers.forEach( initializer::addApplicableTypeName ) );
    }

    public static void registerInitializer( String className ) {
        additionalInitializers.add( className );
    }

    public static void removeInitializer( String className ) {
        additionalInitializers.remove( className );
    }
}