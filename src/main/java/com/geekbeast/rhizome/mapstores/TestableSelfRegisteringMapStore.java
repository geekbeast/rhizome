package com.geekbeast.rhizome.mapstores;

import com.google.common.annotations.VisibleForTesting;


public interface TestableSelfRegisteringMapStore<K, V> extends SelfRegisteringMapStore<K,V> {
    String getMapName();
    String getTable();
    
    @VisibleForTesting
    K generateTestKey();
    
    @VisibleForTesting
    V generateTestValue();
}
