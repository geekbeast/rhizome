package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.kryptnostic.rhizome.hazelcast.serializers.RhizomeUtils;

public class UUIDSet extends HashSet<UUID> {
    private static final long serialVersionUID = 6290405515755142889L;

    public UUIDSet( Collection<UUID> c ) {
        super( RhizomeUtils.Sets.expectedSize( c.size() ) );
        addAll( c );
    }

    public UUIDSet( int initialCapacity ) {
        super( RhizomeUtils.Sets.expectedSize( initialCapacity ) );
    }

    public UUIDSet() {
        super();
    }

    public static UUIDSet of( UUID uuid ) {
        UUIDSet us = new UUIDSet( 1 );
        us.add( uuid );
        return us;
    }
}
