package com.kryptnostic.rhizome.hazelcast.processors;

import java.util.Collection;

import com.google.common.collect.Sets;

public class RemoveFromSetEntryProcessor<K, T> extends AbstractUpdater<K, Collection<T>, T> {

    private static final long serialVersionUID = -8175755464008241279L;

    protected RemoveFromSetEntryProcessor( Iterable<T> objects ) {
        super( objects, SetEntryProcessorOperation.REMOVE );
    }

    @Override
    protected Collection<T> newEmptyCollection() {
        return Sets.newHashSet();
    }
}
