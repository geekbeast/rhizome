package com.kryptnostic.rhizome.cassandra;

import com.datastax.driver.core.BoundStatement;

public interface BindingFunction<K, V> {
    BoundStatement bind( K key, V Value, BoundStatement bs );
}