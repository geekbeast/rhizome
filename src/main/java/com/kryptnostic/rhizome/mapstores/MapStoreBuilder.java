package com.kryptnostic.rhizome.mapstores;

public interface MapStoreBuilder<K, V> {

    TestableSelfRegisteringMapStore<K, V> build();

    MapStoreBuilder<K, V> withTableName( String name );

    MapStoreBuilder<K, V> withMapName( String name );

    MapStoreBuilder<K, V> withTableAndMapName( String name );
}
