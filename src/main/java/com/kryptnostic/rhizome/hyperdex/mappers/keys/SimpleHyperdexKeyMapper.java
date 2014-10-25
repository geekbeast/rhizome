package com.kryptnostic.rhizome.hyperdex.mappers.keys;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;

public class SimpleHyperdexKeyMapper<K> implements HyperdexKeyMapper<K> {
    @Override
    public String getKey(K key) {
        return key.toString();
    }
}
