package com.kryptnostic.rhizome.mapstores;

import com.kryptnostic.rhizome.mappers.SelfRegisteringKeyMapper;
import com.kryptnostic.rhizome.mappers.SelfRegisteringValueMapper;

public abstract class AbstractMapStoreBuilder<K, V> implements MapStoreBuilder<K, V> {
    protected SelfRegisteringValueMapper<V> valueMapper;
    protected SelfRegisteringKeyMapper<K>   keyMapper;
    protected String                        mapName;
    protected String                        tableName;
    public K                                testKey;
    public V                                testValue;
    public boolean                          objectFormat;
    public boolean                          eagerLoading;
    public int                              writeBehind;
    public boolean                          loadAllKeysDisabled;

    public AbstractMapStoreBuilder(
            SelfRegisteringKeyMapper<K> keyMapper,
            SelfRegisteringValueMapper<V> valueMapper ) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.writeBehind = 0;
    }

    @Override
    public MapStoreBuilder<K, V> setObjectInMemoryFormat() {
        this.objectFormat = true;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> enableEagerLoading() {
        this.eagerLoading = true;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> disableLoadAllKeys() {
        this.loadAllKeysDisabled = true;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> enableWriteBehindMode( int writeBehindTime ) {
        this.writeBehind = writeBehindTime;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withMapName( String name ) {
        this.mapName = name;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withTableName( String name ) {
        this.tableName = name;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withTableAndMapName( String tableAndMapName ) {
        this.tableName = tableAndMapName;
        this.mapName = tableAndMapName;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withCustomKeyMapper( SelfRegisteringKeyMapper<K> mapper ) {
        this.keyMapper = mapper;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withCustomValueMapper( SelfRegisteringValueMapper<V> mapper ) {
        this.valueMapper = mapper;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withTestValue( V value ) {
        this.testValue = value;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withTestKey( K key ) {
        this.testKey = key;
        return this;
    }

    @Override
    public abstract TestableSelfRegisteringMapStore<K, V> build();

}
