package com.kryptnostic.rhizome.mapstores;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.hyperdex.pooling.HyperdexClientPool;
import com.kryptnostic.rhizome.mappers.Mappers;
import com.kryptnostic.rhizome.mappers.keys.KeyMappers;

public class ConfigurationHyperdexMapstore extends BaseHyperdexJacksonKeyValueMapStore<ConfigurationKey, String> {
    public ConfigurationHyperdexMapstore( String space, HyperdexClientPool pool ) {
        super( space, pool, KeyMappers.newConfigurationKeyMapper(), Mappers.newStringMapper() );
    }
}
