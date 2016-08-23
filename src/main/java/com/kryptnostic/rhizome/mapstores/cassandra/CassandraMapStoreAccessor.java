package com.kryptnostic.rhizome.mapstores.cassandra;

import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.ListenableFuture;

public interface CassandraMapStoreAccessor<K> {
    public Result<K> getAllKeys();

    public ListenableFuture<Result<K>> getAllKeysAsync();
}
