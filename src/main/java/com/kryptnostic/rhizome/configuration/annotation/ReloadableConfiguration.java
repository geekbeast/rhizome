package com.kryptnostic.rhizome.configuration.annotation;

import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

/**
 * Annotation based way of specifying configuration keys and files.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public @interface ReloadableConfiguration {
    /**
     * @return The file where the configuration should be loaded from if not locatable by key.
     */
    public String file();

    /**
     * @return The key used by the {@link ConfigurationService} to locate the configuration. 
     */
    public String key();
}
