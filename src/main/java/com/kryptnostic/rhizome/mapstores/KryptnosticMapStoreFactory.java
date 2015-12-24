package com.kryptnostic.rhizome.mapstores;

import java.util.Set;

/**
 *
 * @author Drew Bailey drew@kryptnostic.com
 *
 */
public interface KryptnosticMapStoreFactory {

    <K, V> MapStoreBuilder<K, V> build( Class<K> keyType, Class<V> valType );

    <K, C extends Set<V>, V> MapStoreBuilder<K, C> buildSetProxy( Class<K> keyType, Class<V> valType );

}
