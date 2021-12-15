package com.kryptnostic.rhizome.mapstores;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.map.MapStore;

/**
 * Used for implementing self-registration of map stores into HazelcastInstances.
 * 
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 * @param <K> The key type for the map store
 * @param <V> They value type for the map store.
 */
public interface SelfRegisteringMapStore<K, V> extends MapStore<K, V> {
    MapConfig getMapConfig();
    MapStoreConfig getMapStoreConfig();

    default boolean isMetricsEnabled() {
        return true;
    }
}
