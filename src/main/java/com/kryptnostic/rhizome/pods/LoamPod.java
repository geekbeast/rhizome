package com.kryptnostic.rhizome.pods;

import java.io.IOException;

import com.kryptnostic.rhizome.core.Loam;

/**
 * Type marker for accepting different servlet containers (Jetty,Tomcat,etc).
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public interface LoamPod {
    Loam getLoam() throws IOException;
}
