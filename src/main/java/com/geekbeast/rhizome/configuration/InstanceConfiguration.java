package com.geekbeast.rhizome.configuration;

/**
 * An instance configuration is a configuration of a single type that can vary from one instance to another, such as
 * {@link RhizomeConfiguration}
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public interface InstanceConfiguration extends Configuration {
    public String getServiceName();
}
