package com.geekbeast.rhizome.hazelcast.processors;

import java.util.Collection;
import java.util.Map.Entry;

import com.geekbeast.hazelcast.SetProxy;

public class AbstractRemover<K, V extends Collection<T>, T> extends AbstractRhizomeEntryProcessor<K, V, Void> {
    private static final long serialVersionUID = 1500519104651067092L;

    protected final Iterable<T>         objectsToRemove;

    protected AbstractRemover( Iterable<T> objectsToRemove ) {
        this.objectsToRemove = objectsToRemove;
    }

    @Override
    public Void process( Entry<K, V> entry ) {
        V currentObjects = entry.getValue();
        if ( currentObjects != null ) {
            for( T objectToRemove : objectsToRemove ) {
                currentObjects.remove( objectToRemove );
            }
        }

        //Don't trigger re-serialization if handled by SetProxy.
        if ( !( currentObjects instanceof SetProxy<?, ?> ) ) {
            entry.setValue( currentObjects );
        }
        
        //TODO cassandra is not distinguishing null value and empty collection right now; this line should be removed as soon as cassandra fixes NPE in https://github.com/apache/cassandra/pull/98
//        if( currentObjects.isEmpty() ){
//            entry.setValue( null );
//        }

        return null;
    }

    public Iterable<T> getBackingCollection() {
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
