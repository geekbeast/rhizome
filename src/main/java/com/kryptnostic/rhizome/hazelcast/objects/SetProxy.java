package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Set;

public interface SetProxy<K, T> extends Set<T> {

    static final String KEY_COLUMN_NAME   = "setId";
    static final String VALUE_COLUMN_NAME = "data";
}
