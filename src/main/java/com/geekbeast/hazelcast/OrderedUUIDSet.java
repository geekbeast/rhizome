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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.geekbeast.rhizome.hazelcast.serializers.RhizomeUtils;

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
