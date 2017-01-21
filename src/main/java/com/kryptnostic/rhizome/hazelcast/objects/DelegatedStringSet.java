package com.kryptnostic.rhizome.hazelcast.objects;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DelegatedStringSet implements Set<String> {
    private final Set<String> strings;

    public DelegatedStringSet( Set<String> strings ) {
        this.strings = strings;
    }

    public static DelegatedStringSet wrap( Set<String> strings ) {
        return new DelegatedStringSet( strings );
    }

    public Set<String> unwrap() {
        return strings;
    }

    public void forEach( Consumer<? super String> action ) {
        strings.forEach( action );
    }

    public int size() {
        return strings.size();
    }

    public boolean isEmpty() {
        return strings.isEmpty();
    }

    public boolean contains( Object o ) {
        return strings.contains( o );
    }

    public Iterator<String> iterator() {
        return strings.iterator();
    }

    public Object[] toArray() {
        return strings.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return strings.toArray( a );
    }

    public boolean add( String e ) {
        return strings.add( e );
    }

    public boolean remove( Object o ) {
        return strings.remove( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return strings.containsAll( c );
    }

    public boolean addAll( Collection<? extends String> c ) {
        return strings.addAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return strings.retainAll( c );
    }

    public boolean removeAll( Collection<?> c ) {
        return strings.removeAll( c );
    }

    public void clear() {
        strings.clear();
    }

    public boolean equals( Object o ) {
        return strings.equals( o );
    }

    public int hashCode() {
        return strings.hashCode();
    }

    public Spliterator<String> spliterator() {
        return strings.spliterator();
    }

    public boolean removeIf( Predicate<? super String> filter ) {
        return strings.removeIf( filter );
    }

    public Stream<String> stream() {
        return strings.stream();
    }

    public Stream<String> parallelStream() {
        return strings.parallelStream();
    }
}
