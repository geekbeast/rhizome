package com.kryptnostic.rhizome.hazelcast.processors;

import java.util.Map;
import java.util.Map.Entry;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class AbstractRhizomeEntryProcessor<K, V, R>
        implements EntryProcessor<K, V>, EntryBackupProcessor<K, V> {
    private static final long serialVersionUID = 5060655249179605949L;
    private final boolean     applyOnBackup;

    /**
     * Creates an EntryProcessor that applies the {@link #process(java.util.Map.Entry)} to primary and backups.
     */
    public AbstractRhizomeEntryProcessor() {
        this( true );
    }

    @Override
    public abstract R process( Map.Entry<K, V> entry );

    public AbstractRhizomeEntryProcessor( boolean applyOnBackup ) {
        this.applyOnBackup = applyOnBackup;
    }

    @Override
    public final EntryBackupProcessor<K, V> getBackupProcessor() {
        if ( applyOnBackup ) {
            return this;
        }
        return null;
    }

    @Override
    public void processBackup( Entry<K, V> entry ) {
        process( entry );
    }

}
