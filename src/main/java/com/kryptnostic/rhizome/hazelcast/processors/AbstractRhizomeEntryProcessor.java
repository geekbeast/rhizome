package com.kryptnostic.rhizome.hazelcast.processors;

import com.hazelcast.cache.BackupAwareEntryProcessor;
import com.hazelcast.map.EntryProcessor;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class AbstractRhizomeEntryProcessor<K, V, R>
        implements EntryProcessor<K, V, R>, BackupAwareEntryProcessor<K, V, R> {
    private static final long    serialVersionUID = 5060655249179605949L;
    private final        boolean applyOnBackup;

    /**
     * Creates an EntryProcessor that applies the {@link #process(java.util.Map.Entry)} to primary and backups.
     */
    public AbstractRhizomeEntryProcessor() {
        this( true );
    }

    public AbstractRhizomeEntryProcessor( boolean applyOnBackup ) {
        this.applyOnBackup = applyOnBackup;
    }

    @Override
    public abstract R process( @Nonnull Map.Entry<K, V> entry );

    @Override
    public final EntryProcessor<K, V, R> getBackupProcessor() {
        if ( applyOnBackup ) {
            return this;
        }
        return null;
    }

}
