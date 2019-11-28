package com.kryptnostic.rhizome.hazelcast.processors;

import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Drew Bailey drew@kryptnostic.com
 *
 * @param <T>
 */
public abstract class SetContainsEntryProcessor<T> extends AbstractRhizomeEntryProcessor<T, Set<T>, Boolean> {

    private static final long serialVersionUID = 667451566436289867L;

    private final T           object;

    public SetContainsEntryProcessor( T object ) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    @Override
    public Boolean process( Entry<T, Set<T>> entry ) {
        Set<T> value = entry.getValue();
        if ( value == null || value.isEmpty() ) {
            return null;
        }
        return value.contains( getObject() );
    }

}
