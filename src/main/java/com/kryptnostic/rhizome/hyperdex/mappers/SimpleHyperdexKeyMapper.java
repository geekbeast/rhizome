package com.kryptnostic.rhizome.hyperdex.mappers;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;

public class SimpleHyperdexKeyMapper<K> implements HyperdexKeyMapper<K> {
    @Override
    public Object getKey(K key) {
        return key.toString();
    }
}
