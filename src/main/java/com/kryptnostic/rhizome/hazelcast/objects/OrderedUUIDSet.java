package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.kryptnostic.rhizome.hazelcast.serializers.RhizomeUtils;

/**
 * This class extends LinkedHashSet because it is used for consistently returning results
 *      in their original insertion order
 * @author drew
 *
 */
public class OrderedUUIDSet extends LinkedHashSet<UUID> {
    private static final long serialVersionUID = -1385185020931469783L;

    public OrderedUUIDSet( Set<UUID> initialCollection ) {
        super( initialCollection );
    }

    public OrderedUUIDSet() {
        super();
    }

    public OrderedUUIDSet( int initialCapactity ) {
        super( RhizomeUtils.Sets.expectedSize( initialCapactity ) );
    }

    public static OrderedUUIDSet of( UUID uuid ) {
        OrderedUUIDSet ois = new OrderedUUIDSet( 1 );
        ois.add( uuid );
        return ois;
    }
}
