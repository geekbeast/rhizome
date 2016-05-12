package com.kryptnostic.rhizome.configuration.annotation;

/**
 * Marks this as a configuration that is only loadable at startup.
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt; 
 *
 */
public @interface StartupConfiguration {
    /**
     * @return The file where the configuration should be loaded from if not locatable by key.
     */
    String file();
}
