package com.kryptnostic.rhizome.configuration.websockets;

import org.springframework.beans.BeansException;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.kryptnostic.rhizome.core.Rhizome;
import com.kryptnostic.rhizome.pods.LoamPod;

public class BaseRhizomeServer {
    private final Rhizome rhizome;

    public BaseRhizomeServer( Class<?>... classes ) {
        rhizome = new Rhizome( classes );
    }

    public BaseRhizomeServer( Class<? extends LoamPod> loamPodClass, Class<?>... pods ) {
        rhizome = new Rhizome( loamPodClass, pods );
    }
    
    public void intercrop( Class<?>... pods ) {
        rhizome.intercrop( pods );
    }

    public void start( String... profiles ) throws Exception {
        rhizome.sprout( profiles );
    }

    public void stop() throws BeansException, Exception {
        rhizome.wilt();
    }

    public AnnotationConfigWebApplicationContext getContext() {
        return rhizome.getContext();
    }
}
