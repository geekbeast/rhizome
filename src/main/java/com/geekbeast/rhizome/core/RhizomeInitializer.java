package com.geekbeast.rhizome.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * All plugins in the Rhizome framework should extend from the {@code Rhizome} class.
 * {@code Rhizome} implements the {@code WebApplicationInitializer} class, which causes
 * it to be automatically loaded by the Servlet 3.1 API.  Plugin should register their 
 * {@code @Configuration} annotated beans via calls to {@code openPods(...)} and should 
 * generally not override onStartup unless they need to register additional special servlets.
 * @author Matthew Tamayo-Rios
 */
public class RhizomeInitializer implements WebApplicationInitializer {
    private static final AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    
    public final void openPods( Class<?> ... pods ) { 
        rootContext.register( pods );
    }

    @Override
    public void onStartup( ServletContext sc ) throws ServletException {
        //Do Nothing 
    }
}
