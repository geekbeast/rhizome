package com.kryptnostic.rhizome.hazelcast.objects;

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
