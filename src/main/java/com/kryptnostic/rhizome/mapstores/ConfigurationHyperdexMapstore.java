package com.kryptnostic.rhizome.mapstores;

import org.hyperdex.client.Client;

import com.geekbeast.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.hyperdex.mappers.HyperdexMappers;
import com.kryptnostic.rhizome.hyperdex.mappers.keys.HyperdexKeyMappers;

public class ConfigurationHyperdexMapstore extends BaseHyperdexJacksonKeyValueMapStore<ConfigurationKey, String> {
    public ConfigurationHyperdexMapstore( String space, Client client ) {
        super( space, client, HyperdexKeyMappers.newConfigurationKeyMapper() , HyperdexMappers.newStringMapper() );
    }
}
