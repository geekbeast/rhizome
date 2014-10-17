package com.geekbeast.rhizome.configuration.hyperdex;

import java.lang.reflect.Field;

import com.geekbeast.rhizome.pods.ConfigurationPod;
import com.google.common.base.Optional;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 * This class must be instantiated before any hyperdex jar calls are made, otherwise loading the hyperdex libraries will fail.
 */
public final class HyperdexPreconfigurer {
    private static final String DEFAULT_HYPERDEX_LIB_DIRECTORY = "lib/centos:lib/macosx";
    
    static {
        Optional<HyperdexConfiguration> hyperdexConfiguration = ConfigurationPod.getRhizomeConfiguration().getHyperdexConfiguration();
        String hyperdexLibDirectory = hyperdexConfiguration.isPresent() ? hyperdexLibDirectory = hyperdexConfiguration.get().getNativeBinPath() : DEFAULT_HYPERDEX_LIB_DIRECTORY;
        String property = System.getProperty("java.library.path");
        property = property.concat(":"+hyperdexLibDirectory);
        System.setProperty("java.library.path", property);

        Field fsp;
        try {
            fsp = ClassLoader.class.getDeclaredField("sys_paths");
            fsp.setAccessible(true);
            fsp.set(null, null);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private HyperdexPreconfigurer() {}
    
    public static void configure() {
        
    }
}
