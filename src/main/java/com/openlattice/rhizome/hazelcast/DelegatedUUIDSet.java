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

package com.openlattice.rhizome.hazelcast;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DelegatedUUIDSet implements Set<UUID> {
    private final Set<UUID>   uuids;

    public DelegatedUUIDSet( Set<UUID> uuids ) {
        this.uuids = uuids;
    }

    public static DelegatedUUIDSet wrap( Set<UUID> uuids ) {
        return new DelegatedUUIDSet( uuids );
    }

    public Set<UUID> unwrap() {
        return uuids;
    }

    public void forEach( Consumer<? super UUID> action ) {
        uuids.forEach( action );
    }

    public int size() {
        return uuids.size();
    }

    public boolean isEmpty() {
        return uuids.isEmpty();
    }

    public boolean contains( Object o ) {
        return uuids.contains( o );
    }

    public Iterator<UUID> iterator() {
        return uuids.iterator();
    }

    public Object[] toArray() {
        return uuids.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return uuids.toArray( a );
    }

    public boolean add( UUID e ) {
        return uuids.add( e );
    }

    public boolean remove( Object o ) {
        return uuids.remove( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return uuids.containsAll( c );
    }

    public boolean addAll( Collection<? extends UUID> c ) {
        return uuids.addAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return uuids.retainAll( c );
    }

    public boolean removeAll( Collection<?> c ) {
        return uuids.removeAll( c );
    }

    public void clear() {
        uuids.clear();
    }

    public boolean equals( Object o ) {
        return uuids.equals( o );
    }

    public int hashCode() {
        return uuids.hashCode();
    }

    public Spliterator<UUID> spliterator() {
        return uuids.spliterator();
    }

    public boolean removeIf( Predicate<? super UUID> filter ) {
        return uuids.removeIf( filter );
    }

    public Stream<UUID> stream() {
        return uuids.stream();
    }

    public Stream<UUID> parallelStream() {
        return uuids.parallelStream();
    }
}
