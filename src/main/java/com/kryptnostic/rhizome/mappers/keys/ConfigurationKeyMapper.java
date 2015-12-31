package com.kryptnostic.rhizome.mappers.keys;

import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.SimpleConfigurationKey;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;

public class ConfigurationKeyMapper implements SelfRegisteringKeyMapper<ConfigurationKey> {

    @Override
    public String fromKey( ConfigurationKey key ) {
        return key.getUri();
    }

    @Override
    public ConfigurationKey toKey( String value ) {
        return new SimpleConfigurationKey( value );
    }

    @Override
    public Class<ConfigurationKey> getClazz() {
        return ConfigurationKey.class;
    }
}
