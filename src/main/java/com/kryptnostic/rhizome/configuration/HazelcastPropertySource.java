package com.kryptnostic.rhizome.configuration;

import org.springframework.core.env.PropertySource;

import com.hazelcast.core.IMap;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
public class HazelcastPropertySource extends PropertySource<IMap<String, Object>> {

    public HazelcastPropertySource( String name, IMap<String, Object> source ) {
        super( name, source );
    }

    @Override
    public Object getProperty( String propertyName ) {
        return source.get( name );
    }

}
