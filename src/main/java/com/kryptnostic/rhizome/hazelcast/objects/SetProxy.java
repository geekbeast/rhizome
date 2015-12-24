package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Set;

public interface SetProxy<K, T> extends Set<T> {

    public static final String KEY_COLUMN_NAME   = "setId";
    public static final String VALUE_COLUMN_NAME = "data";

    Class<T> getTypeClazz();
}
