package com.kryptnostic.rhizome.mappers;

public interface SelfRegisteringValueMapper<T> extends ValueMapper<T> {
    Class<? extends T> getClazz();
}
