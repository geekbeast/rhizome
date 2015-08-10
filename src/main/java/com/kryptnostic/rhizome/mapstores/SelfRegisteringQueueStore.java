package com.kryptnostic.rhizome.mapstores;

import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.QueueStore;

/**
 * Used for implementing self-registration of map stores into HazelcastInstances.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 * @param <K> The key type for the map store
 * @param <V> They value type for the map store.
 */
public interface SelfRegisteringQueueStore<T> extends QueueStore<T> {
    QueueConfig getQueueConfig();
}
