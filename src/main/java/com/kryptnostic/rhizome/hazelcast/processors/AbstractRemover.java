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
}
