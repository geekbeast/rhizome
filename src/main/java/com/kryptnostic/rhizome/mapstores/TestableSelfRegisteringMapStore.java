package com.kryptnostic.rhizome.mapstores;


public interface TestableSelfRegisteringMapStore<K, V> extends SelfRegisteringMapStore<K,V> {
    K generateTestKey();
    V generateTestValue() throws Exception;
}
