package com.kryptnostic.rhizome.hyperdex.mappers.keys;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;

public class ConfigurationKeyMapper implements HyperdexKeyMapper<ConfigurationKey> {

    @Override
    public String getKey( ConfigurationKey key ) {
        return key.getUri();
    }

}
