package com.kryptnostic.rhizome.mapstores.hyperdex;

import org.apache.commons.lang3.RandomStringUtils;

import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.SimpleConfigurationKey;
import com.kryptnostic.rhizome.mappers.Mappers;
import com.kryptnostic.rhizome.mappers.keys.ConfigurationKeyMapper;
import com.kryptnostic.rhizome.pooling.hyperdex.HyperdexClientPool;

public class HyperdexConfigurationMapstore extends HyperdexBaseJacksonKeyValueMapStore<ConfigurationKey, String> {
    public HyperdexConfigurationMapstore( String mapName, String space, HyperdexClientPool pool ) {
        super( mapName, space, pool, new ConfigurationKeyMapper(), Mappers.newStringMapper() );
    }

    @Override
    public ConfigurationKey generateTestKey() {
        return new SimpleConfigurationKey( RandomStringUtils.randomAlphanumeric( 10 ) );
    }

    @Override
    public String generateTestValue() throws Exception {
        return RandomStringUtils.randomAlphanumeric( 10 );
    }
}
