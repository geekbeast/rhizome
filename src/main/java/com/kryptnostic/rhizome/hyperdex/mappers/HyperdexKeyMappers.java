package com.kryptnostic.rhizome.hyperdex.mappers;


public final class HyperdexKeyMappers {
    private HyperdexKeyMappers() {}
    public static StringHyperdexKeyMapper newStringPassthrough() {
        return new StringHyperdexKeyMapper();
    }
}
