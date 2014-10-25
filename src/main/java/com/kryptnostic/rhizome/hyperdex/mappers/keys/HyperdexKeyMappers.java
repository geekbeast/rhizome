package com.kryptnostic.rhizome.hyperdex.mappers.keys;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;
import com.kryptnostic.rhizome.hyperdex.mappers.SimpleHyperdexKeyMapper;

public final class HyperdexKeyMappers {
    private static final ConfigurationKeyMapper configurationKeyMapper = new ConfigurationKeyMapper();
    private HyperdexKeyMappers() {}

    public static StringHyperdexKeyMapper newStringPassthrough() {
        return new StringHyperdexKeyMapper();
    }

    public static <K> HyperdexKeyMapper<K> newSimpleHyperdexKeyMapper() {
        return new SimpleHyperdexKeyMapper<K>();
    }

    public static ConfigurationKeyMapper newConfigurationKeyMapper() {
        return configurationKeyMapper;
    }
}
