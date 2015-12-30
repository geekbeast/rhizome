package com.kryptnostic.rhizome.mapstores;

import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;

public interface MapStoreBuilder<K, V> {

    TestableSelfRegisteringMapStore<K, V> build();

    MapStoreBuilder<K, V> withTableName( String name );

    MapStoreBuilder<K, V> withMapName( String name );

    MapStoreBuilder<K, V> withTableAndMapName( String name );

    MapStoreBuilder<K, V> withCustomKeyMapper( SelfRegisteringKeyMapper<K> mapper );

    MapStoreBuilder<K, V> withCustomValueMapper( SelfRegisteringValueMapper<V> mapper );

    MapStoreBuilder<K, V> setObjectInMemoryFormat();
}
