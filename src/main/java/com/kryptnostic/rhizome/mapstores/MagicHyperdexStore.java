package com.kryptnostic.rhizome.mapstores;

import org.hyperdex.client.Client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kryptnostic.rhizome.hyperdex.mappers.keys.JacksonHyperdexKeyMapper;
import com.kryptnostic.rhizome.hyperdex.mappers.values.TypeReferenceHyperdexMapper;

public class MagicHyperdexStore<K,V> extends BaseHyperdexJacksonKeyValueMapStore<K, V>{

    public MagicHyperdexStore( String space, Client client, TypeReference<V> reference ) {
        super( space, client, new JacksonHyperdexKeyMapper<K>(), new TypeReferenceHyperdexMapper<V>( reference ) );
    }

}
