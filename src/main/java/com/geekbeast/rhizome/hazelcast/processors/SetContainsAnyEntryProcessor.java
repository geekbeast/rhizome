package com.geekbeast.rhizome.hazelcast.processors;

import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

public abstract class SetContainsAnyEntryProcessor<T> extends AbstractRhizomeEntryProcessor<T, Set<T>, Boolean> {

    private static final long serialVersionUID = -1578286848277056995L;
    private final Set<T>      objectsToCheck;

    public SetContainsAnyEntryProcessor( Set<T> objectsToCheck ) {
        super( false /* don't apply on backup nodes */ );
        this.objectsToCheck = objectsToCheck;
    }

    public Set<T> getObjectsToCheck() {
        return objectsToCheck;
    }

    @Override
    public Boolean process( Entry<T, Set<T>> entry ) {
        Set<T> value = entry.getValue();
        if ( value == null || value.isEmpty() ) {
            return null;
        }
        return !Sets.intersection( value, objectsToCheck ).isEmpty();
    }

}
