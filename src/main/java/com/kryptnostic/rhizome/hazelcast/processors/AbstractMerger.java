package com.kryptnostic.rhizome.hazelcast.processors;

import java.util.Collection;
import java.util.Map.Entry;

import com.openlattice.rhizome.hazelcast.SetProxy;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 * @param <K> The type for the key in the underlying hazelcast map.
 * @param <V> The container type for the value in the underlying hazelcast map.
 * @param <T> The type for the values in container {@code V extends Collection<T>}
 */
public abstract class AbstractMerger<K, V extends Collection<T>, T>
        extends AbstractRhizomeEntryProcessor<K, V, Void> {
    private static final long   serialVersionUID = 4022386342619821133L;

    protected final Iterable<T> newObjects;

    protected AbstractMerger( Iterable<T> objects ) {
        this.newObjects = objects;
    }

    @Override
    public Void process( Entry<K, V> entry ) {
        V currentObjects = entry.getValue();
        if ( currentObjects == null ) {
            currentObjects = newEmptyCollection();
        }

        newObjects.forEach( currentObjects::add );
        processBeforeWriteBack( currentObjects );
        //Don't trigger re-serialization if handled by SetProxy.
        if ( !( currentObjects instanceof SetProxy<?, ?> ) ) {
            entry.setValue( currentObjects );
        }
        return null;
    }

    protected void processBeforeWriteBack( V value ) {

    }

    public Iterable<T> getBackingCollection() {
        return newObjects;
    }

    protected abstract V newEmptyCollection();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( newObjects == null ) ? 0 : newObjects.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( !( obj instanceof AbstractMerger ) ) {
            return false;
        }
        @SuppressWarnings( "rawtypes" )
        AbstractMerger other = (AbstractMerger) obj;
        if ( newObjects == null ) {
            if ( other.newObjects != null ) {
                return false;
            }
        } else if ( !newObjects.equals( other.newObjects ) ) {
            return false;
        }
        return true;
    }

}
