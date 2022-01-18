package com.geekbeast.rhizome.hazelcast.processors;

import java.util.Collection;

import com.google.common.collect.Sets;

public class AddToSetEntryProcessor<K, T> extends AbstractUpdater<K, Collection<T>, T> {

    private static final long serialVersionUID = -5935529020419227114L;

    protected AddToSetEntryProcessor( Iterable<T> objects ) {
        super( objects, SetEntryProcessorOperation.ADD );
    }

    @Override
    protected Collection<T> newEmptyCollection() {
        return Sets.newHashSet();
    }
}
