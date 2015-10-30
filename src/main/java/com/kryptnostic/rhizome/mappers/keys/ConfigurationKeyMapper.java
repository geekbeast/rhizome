package com.kryptnostic.rhizome.mappers.keys;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.geekbeast.rhizome.configuration.SimpleConfigurationKey;
import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mapstores.MappingException;

public class ConfigurationKeyMapper implements SelfRegisteringKeyMapper<ConfigurationKey> {

    @Override
    public String fromKey( ConfigurationKey key ) {
        return key.getUri();
    }

    @Override
    public ConfigurationKey toKey( String value ) throws MappingException {
        return new SimpleConfigurationKey( value );
    }

    @Override
    public Class<ConfigurationKey> getClazz() {
        return ConfigurationKey.class;
    }
}
