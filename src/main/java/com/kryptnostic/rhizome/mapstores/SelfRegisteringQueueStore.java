package com.kryptnostic.rhizome.mapstores;

import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.QueueStoreConfig;
import com.hazelcast.core.QueueStore;

/**
 * Used for implementing self-registration of queue stores into HazelcastInstances.
 *
 * @author Julianna Lamb &lt;julianna@kryptnostic.com&gt;
 *
 * @param <T> The type for the queue store
 */
public interface SelfRegisteringQueueStore<T> extends QueueStore<T> {
    QueueConfig getQueueConfig();

    QueueStoreConfig getQueueStoreConfig();
}
