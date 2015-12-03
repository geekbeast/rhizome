package com.kryptnostic.rhizome.mapstores;

import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;

public interface MapStoreBuilder<K, V> {

    TestableSelfRegisteringMapStore<K, V> build();

    MapStoreBuilder<K, V> withTableName( String name );

    MapStoreBuilder<K, V> withMapName( String name );

    MapStoreBuilder<K, V> withTableAndMapName( String name );

    MapStoreBuilder<K, V> withCustomKeyMapper( KeyMapper<K> mapper );

    MapStoreBuilder<K, V> withCustomValueMapper( ValueMapper<V> mapper );

    MapStoreBuilder<K, V> setObjectInMemoryFormat();
}
