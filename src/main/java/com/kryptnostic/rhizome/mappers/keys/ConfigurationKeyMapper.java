package com.kryptnostic.rhizome.mappers.keys;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.hyperdex.MapStoreKeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class ConfigurationKeyMapper implements MapStoreKeyMapper<ConfigurationKey> {

    @Override
    public String getKey( ConfigurationKey key ) {
        return key.getUri();
    }

    @Override
    public ConfigurationKey fromString( String str ) throws MappingException {
        throw new UnsupportedOperationException( this.getClass().getCanonicalName() + " not implemented" );
    }

}
