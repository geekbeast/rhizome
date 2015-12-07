package com.kryptnostic.rhizome.mapstores;

import com.kryptnostic.rhizome.mappers.KeyMapper;
import com.kryptnostic.rhizome.mappers.ValueMapper;

public abstract class AbstractMapStoreBuilder<K, V> implements MapStoreBuilder<K, V> {
    protected ValueMapper<V> valueMapper;
    protected KeyMapper<K>   keyMapper;
    protected String               mapName;
    protected String               tableName;
    public boolean           objectFormat;

    public AbstractMapStoreBuilder(
            KeyMapper<K> keyMapper,
            ValueMapper<V> valueMapper ) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;

    }

    @Override
    public MapStoreBuilder<K, V> setObjectInMemoryFormat() {
        this.objectFormat = true;
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
    public MapStoreBuilder<K, V> withCustomKeyMapper( KeyMapper<K> mapper ) {
        this.keyMapper = mapper;
        return this;
    }

    @Override
    public MapStoreBuilder<K, V> withCustomValueMapper( ValueMapper<V> mapper ) {
        this.valueMapper = mapper;
        return this;
    }

    @Override
    public abstract TestableSelfRegisteringMapStore<K, V> build();
}
