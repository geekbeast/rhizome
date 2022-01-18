/*
 * Copyright (C) 2017. OpenLattice, Inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 */

package com.geekbeast.hazelcast;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.geekbeast.rhizome.hazelcast.serializers.RhizomeUtils;

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
