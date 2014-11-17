package com.kryptnostic.rhizome.mapstores;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMappers;
import com.kryptnostic.rhizome.hyperdex.mappers.keys.HyperdexKeyMappers;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;

public class ConfigurationHyperdexMapstore extends BaseHyperdexJacksonKeyValueMapStore<ConfigurationKey, String> {
    public ConfigurationHyperdexMapstore( String space, HyperdexClientPool pool ) {
        super( space, pool, HyperdexKeyMappers.newConfigurationKeyMapper(), HyperdexMappers.newStringMapper() );
    }
}
