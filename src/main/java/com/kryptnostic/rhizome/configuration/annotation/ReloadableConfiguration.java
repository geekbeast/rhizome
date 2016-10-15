package com.kryptnostic.rhizome.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation based way of specifying configuration keys and files.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReloadableConfiguration {
    /**
     * @return The file where the configuration should be loaded from if not locatable by key.
     */
    public String uri() default "";
}
