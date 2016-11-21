package com.kryptnostic.rhizome.cassandra;

import com.datastax.driver.core.BoundStatement;

public interface KeyBindingFunction<K> {
    BoundStatement bind( K key, BoundStatement bs );
}
