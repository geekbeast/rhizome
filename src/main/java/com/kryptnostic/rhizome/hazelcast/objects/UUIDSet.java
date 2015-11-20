package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.google.common.primitives.Ints;

public class UUIDSet extends HashSet<UUID> {
    private static final long serialVersionUID = 6290405515755142889L;

    public UUIDSet( Collection<UUID> c ) {
        super( c );
    }

    public UUIDSet( int initialCapacity ) {
        super( expectedSize( initialCapacity ) );
    }

    public UUIDSet() {
        super();
    }

    public static int expectedSize( int expectedSize ) {
        if ( expectedSize < 0 ) {
            throw new IllegalArgumentException( "expectedSize cannot be negative but was: " + expectedSize );
        }
        if ( expectedSize < 3 ) {
            return expectedSize + 1;
        }
        if ( expectedSize < Ints.MAX_POWER_OF_TWO ) {
            return expectedSize + expectedSize / 3;
        }
        return Integer.MAX_VALUE;
    }

    public static UUIDSet of( UUID uuid ) {
        UUIDSet us = new UUIDSet( 1 );
        us.add( uuid );
        return us;
    }
}
