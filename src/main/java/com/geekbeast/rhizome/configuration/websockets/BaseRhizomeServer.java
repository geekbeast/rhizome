package com.geekbeast.rhizome.configuration.websockets;

import com.geekbeast.rhizome.core.Rhizome;
import com.geekbeast.rhizome.pods.LoamPod;
import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractApplicationContext;

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

    public AbstractApplicationContext getContext() {
        return rhizome.getContext();
    }
}
