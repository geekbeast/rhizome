package com.kryptnostic.rhizome.mappers.keys;

import com.geekbeast.rhizome.configuration.hyperdex.MapStoreKeyMapper;

public final class KeyMappers {
    private static final ConfigurationKeyMapper configurationKeyMapper = new ConfigurationKeyMapper();

    private KeyMappers() {}

    public static StringKeyMapper newStringPassthrough() {
        return new StringKeyMapper();
    }

    public static <K extends String> MapStoreKeyMapper<K> newSimpleHyperdexKeyMapper() {
        return new SimpleKeyMapper<K>();
    }

    public static ConfigurationKeyMapper newConfigurationKeyMapper() {
        return configurationKeyMapper;
    }
}
