package com.geekbeast.rhizome.configuration.websockets;

import org.springframework.beans.BeansException;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.geekbeast.rhizome.core.Rhizome;

public class RhizomeServerBase {
    private final Rhizome rhizome;

    public RhizomeServerBase( Class<?>... classes ) {
        rhizome = new Rhizome( classes );
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
