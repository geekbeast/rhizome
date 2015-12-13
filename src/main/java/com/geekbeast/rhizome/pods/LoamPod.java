package com.geekbeast.rhizome.pods;

import java.io.IOException;

import com.geekbeast.rhizome.core.Loam;

/**
 * Type marker for accepting different servlet containers (Jetty,Tomcat,etc).
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public interface LoamPod {
    Loam getLoam() throws IOException;
}
