package com.kryptnostic.rhizome.mappers;

public interface SelfRegisteringKeyMapper<K> extends KeyMapper<K> {
    Class<K> getClazz();
}
