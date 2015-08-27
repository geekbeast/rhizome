package com.kryptnostic.rhizome.hazelcast.processors;

import java.util.Collection;
import java.util.Map.Entry;

import com.hazelcast.map.AbstractEntryProcessor;

public abstract class AbstractMerger<K, V extends Collection<T>, T> extends AbstractEntryProcessor<K, V> {
    private static final long serialVersionUID = 4022386342619821133L;

    private final V           newObjects;

    protected AbstractMerger( V objects ) {
        this.newObjects = objects;
    }

    @Override
    public Object process( Entry<K, V> entry ) {
        V currentObjects = entry.getValue();
        if ( currentObjects == null ) {
            currentObjects = newEmptyCollection();
        }
        currentObjects.addAll( newObjects );
        entry.setValue( currentObjects );
        return null;
    }

    public V getBackingCollection() {
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
