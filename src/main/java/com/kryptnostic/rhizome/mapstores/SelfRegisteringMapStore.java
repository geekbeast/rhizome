package com.kryptnostic.rhizome.mapstores;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.MapStore;

/**
 * Used for implementing self-registration of map stores into HazelcastInstances.
 *
 * @param <K> The key type for the map store
 * @param <V> They value type for the map store.
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public interface SelfRegisteringMapStore<K, V> extends MapStore<K, V> {

    MapConfig getMapConfig();

    MapStoreConfig getMapStoreConfig();
}
