package com.kryptnostic.rhizome.mapstores;


public interface TestableSelfRegisteringMapStore<K, V> extends SelfRegisteringMapStore<K,V> {
    String getMapName();
    String getTable();
    K generateTestKey();
    V generateTestValue() throws Exception;
}
