package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class UUIDSet extends HashSet<UUID> {
    private static final long serialVersionUID = 6290405515755142889L;

    public UUIDSet( Collection<UUID> c ) {
        super( c );
    }

    public UUIDSet( int initialCapacity ) {
        super( initialCapacity );
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
