package com.kryptnostic.rhizome.mapstores;

import com.kryptnostic.rhizome.hazelcast.objects.SetProxy;

/**
 *
 * @author Drew Bailey
 *
 */
public interface KryptnosticMapStoreFactory {

    <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType );

    <K, C extends SetProxy<K, V>, V> MapStoreBuilder<K, C> buildSetProxy( Class<K> keyType, Class<V> valType );

}
