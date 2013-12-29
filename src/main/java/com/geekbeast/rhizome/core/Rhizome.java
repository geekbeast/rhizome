package com.geekbeast.rhizome.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Matthew Tamayo-Rios
 */
public class RhizomeInitializer implements WebApplicationInitializer {
    protected static final AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    
    public void register( Class<?> ... annotatedClasses ) { 
        rootContext.register( annotatedClasses );
    }

    @Override
    public void onStartup( ServletContext sc ) throws ServletException {
        // TODO Auto-generated method stub
        
    }
}
