package com.kryptnostic.rhizome.hazelcast.objects;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class DelegatedUUIDList implements List<UUID>  {
    private final List<UUID> uuids;

    public DelegatedUUIDList( List<UUID> uuids ) {
        this.uuids = checkNotNull( uuids );
    }

    public  DelegatedUUIDList( UUID... uuids ) {
        this( Arrays.asList( uuids ) );
    }

    public List<UUID> unwrap() {
        return uuids;
    }

    @Override public int size() {
        return uuids.size();
    }

    @Override public boolean isEmpty() {
        return uuids.isEmpty();
    }

    @Override public boolean contains( Object o ) {
        return uuids.contains( o );
    }

    @Override public Iterator<UUID> iterator() {
        return uuids.iterator();
    }

    @Override public Object[] toArray() {
        return uuids.toArray();
    }

    @Override public <T> T[] toArray( T[] a ) {
        return uuids.toArray( a );
    }

    @Override public boolean add( UUID uuid ) {
        return uuids.add( uuid );
    }

    @Override public boolean remove( Object o ) {
        return uuids.remove( o );
    }

    @Override public boolean containsAll( Collection<?> c ) {
        return uuids.containsAll( c );
    }

    @Override public boolean addAll( Collection<? extends UUID> c ) {
        return uuids.addAll( c );
    }

    @Override public boolean addAll( int index, Collection<? extends UUID> c ) {
        return uuids.addAll( index, c );
    }

    @Override public boolean removeAll( Collection<?> c ) {
        return uuids.removeAll( c );
    }

    @Override public boolean retainAll( Collection<?> c ) {
        return uuids.retainAll( c );
    }

    @Override public void replaceAll( UnaryOperator<UUID> operator ) {
        uuids.replaceAll( operator );
    }

    @Override public void sort( Comparator<? super UUID> c ) {
        uuids.sort( c );
    }

    @Override public void clear() {
        uuids.clear();
    }

    @Override public boolean equals( Object o ) {
        return uuids.equals( o );
    }

    @Override public int hashCode() {
        return uuids.hashCode();
    }

    @Override public UUID get( int index ) {
        return uuids.get( index );
    }

    @Override public UUID set( int index, UUID element ) {
        return uuids.set( index, element );
    }

    @Override public void add( int index, UUID element ) {
        uuids.add( index, element );
    }

    @Override public UUID remove( int index ) {
        return uuids.remove( index );
    }

    @Override public int indexOf( Object o ) {
        return uuids.indexOf( o );
    }

    @Override public int lastIndexOf( Object o ) {
        return uuids.lastIndexOf( o );
    }

    @Override public ListIterator<UUID> listIterator() {
        return uuids.listIterator();
    }

    @Override public ListIterator<UUID> listIterator( int index ) {
        return uuids.listIterator( index );
    }

    @Override public List<UUID> subList( int fromIndex, int toIndex ) {
        return uuids.subList( fromIndex, toIndex );
    }

    @Override public Spliterator<UUID> spliterator() {
        return uuids.spliterator();
    }

    @Override public boolean removeIf( Predicate<? super UUID> filter ) {
        return uuids.removeIf( filter );
    }

    @Override public Stream<UUID> stream() {
        return uuids.stream();
    }

    @Override public Stream<UUID> parallelStream() {
        return uuids.parallelStream();
    }

    @Override public void forEach( Consumer<? super UUID> action ) {
        uuids.forEach( action );
    }

    public static DelegatedUUIDList wrap( List<UUID> uuids ) {
        return new DelegatedUUIDList( uuids );
    }
}
