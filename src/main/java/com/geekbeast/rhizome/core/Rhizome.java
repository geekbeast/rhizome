package com.geekbeast.rhizome.core;

import org.springframework.beans.BeansException;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.geekbeast.rhizome.pods.AsyncPod;
import com.geekbeast.rhizome.pods.ConfigurationPod;
import com.geekbeast.rhizome.pods.HazelcastPod;
import com.geekbeast.rhizome.pods.MetricsPod;
import com.geekbeast.rhizome.pods.ServletContainerPod;

public class Rhizome {
    private static final AnnotationConfigWebApplicationContext rootContext = RhizomeWebApplicationInitializer.getContext();
    
    public Rhizome() {
        initialize();
    }
    
    public static void main( String[] args ) throws Exception {
        Rhizome rhizome = new Rhizome();
        rhizome.sprout();
    }
    
    public <T> T harvest( Class<T> clazz ) {
        return rootContext.getBean( clazz );
    }
    
    public void intercrop( Class<?> ... pods ) {
        rootContext.register( pods );
    }
    
    public void sprout() throws Exception {
        rootContext.refresh();
        for( Loam loam : rootContext.getBeansOfType( Loam.class ).values() ) {
            loam.start();
        }
    }
    
    public void wilt() throws BeansException, Exception {
        for( Loam loam : rootContext.getBeansOfType( Loam.class ).values() ) {
            loam.stop();
        }
    }
    
    /**
     * This method should be overridden if any of the built-in defaults are not desired. 
     * To add additional configurations beyond the built in defaults, {@code plant(...)} 
     * should be called to register @Configuration bootstrap beans. 
     */
    protected void initialize() {
        rootContext.register( ConfigurationPod.class );
        rootContext.register( MetricsPod.class );
        rootContext.register( AsyncPod.class );
        rootContext.register( HazelcastPod.class );
        rootContext.register( ServletContainerPod.class );
    }

}
