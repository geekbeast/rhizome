package com.kryptnostic.rhizome.mapstores.hyperdex;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.mappers.Mappers;
import com.kryptnostic.rhizome.mappers.keys.ConfigurationKeyMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public class HyperdexConfigurationMapstore extends HyperdexBaseJacksonKeyValueMapStore<ConfigurationKey, String> {
    public HyperdexConfigurationMapstore( String space, HyperdexClientPool pool ) {
        super( space, pool, new ConfigurationKeyMapper(), Mappers.newStringMapper() );
    }
}
