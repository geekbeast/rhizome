package com.kryptnostic.rhizome.hazelcast.processors;

import java.util.Collection;
import java.util.Map.Entry;

import com.hazelcast.map.AbstractEntryProcessor;

public class AbstractRemover<K, V extends Collection<T>, T> extends AbstractEntryProcessor<K, V> {
    private static final long serialVersionUID = 1500519104651067092L;

    protected final V         objectsToRemove;

    protected AbstractRemover( V objectsToRemove ) {
        this.objectsToRemove = objectsToRemove;
    }

    @Override
    public Object process( Entry<K, V> entry ) {
        V currentObjects = entry.getValue();
        if ( currentObjects != null ) {
            currentObjects.removeAll( objectsToRemove );
        }

        entry.setValue( currentObjects );
        return null;
    }

    public V getBackingCollection() {
        return objectsToRemove;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( objectsToRemove == null ) ? 0 : objectsToRemove.hashCode() );
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
        if ( !( obj instanceof AbstractRemover ) ) {
            return false;
        }
        AbstractRemover<?, ?, ?> other = (AbstractRemover<?, ?, ?>) obj;
        if ( objectsToRemove == null ) {
            if ( other.objectsToRemove != null ) {
                return false;
            }
        } else if ( !objectsToRemove.equals( other.objectsToRemove ) ) {
            return false;
        }
        return true;
    }

}