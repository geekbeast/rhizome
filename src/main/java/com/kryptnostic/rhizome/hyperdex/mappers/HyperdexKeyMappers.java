package com.kryptnostic.rhizome.hyperdex.mappers;

import com.geekbeast.rhizome.configuration.hyperdex.HyperdexKeyMapper;

public final class HyperdexKeyMappers {
    private HyperdexKeyMappers() {
    }

    public static StringHyperdexKeyMapper newStringPassthrough() {
        return new StringHyperdexKeyMapper();
    }

    public static <K> HyperdexKeyMapper<K> newSimpleHyperdexKeyMapper() {
        return new SimpleHyperdexKeyMapper<K>();
    }
}
