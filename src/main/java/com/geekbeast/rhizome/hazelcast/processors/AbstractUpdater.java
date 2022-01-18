package com.geekbeast.rhizome.hazelcast.processors;

import com.geekbeast.hazelcast.SetProxy;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.BiFunction;

public abstract class AbstractUpdater<K, V extends Collection<T>, T>
        extends AbstractRhizomeEntryProcessor<K, V, Void> {

    private static final long                  serialVersionUID = -5387091209847658668L;

    protected final Iterable<T>                objectsToUpdate;

    protected final SetEntryProcessorOperation operation;

    public enum SetEntryProcessorOperation {
        ADD,
        REMOVE,
        APPLY;
    }

    protected AbstractUpdater( Iterable<T> objects, SetEntryProcessorOperation operation ) {
        this.objectsToUpdate = objects;
        this.operation = operation;
    }

    public static <K, V extends Collection<T>, T> AbstractUpdater<K, Collection<T>, T> addToSetUpdater(
            Iterable<T> collection ) {
        return new AddToSetEntryProcessor<>( collection );
    }

    public static <K, V extends Collection<T>, T> AbstractUpdater<K, Collection<T>, T> removeFromSetUpdater(
            Iterable<T> collection ) {
        return new RemoveFromSetEntryProcessor<>( collection );
    }

    protected BiFunction<V, T, Boolean> removeFunction() {
        return Collection::remove;
    }

    protected BiFunction<V, T, Boolean> addFunction() {
        return Collection::add;
    }

    @Override
    public Void process( Entry<K, V> entry ) {
        V currentObjects = entry.getValue();
        if ( currentObjects == null ) {
            currentObjects = newEmptyCollection();
        }

        BiFunction<V, T, Boolean> addOrRemoveFunction;

        switch ( operation ) {
            case ADD:
                addOrRemoveFunction = addFunction();
                break;
            case REMOVE:
                addOrRemoveFunction = removeFunction();
                break;
            case APPLY:
                addOrRemoveFunction = applyFunction();
                break;
            default:
                System.err.println( "Impossible, no operation specified in AbstractUpdater" );
                return null;
        }

        for ( T object : objectsToUpdate ) {
            addOrRemoveFunction.apply( currentObjects, object );
        }

        // Don't trigger re-serialization if handled by SetProxy.
        if ( !( currentObjects instanceof SetProxy<?, ?> ) ) {
            entry.setValue( currentObjects );
        }
        return null;
    }

    public Iterable<T> getBackingCollection() {
        return objectsToUpdate;
    }

    protected abstract V newEmptyCollection();

    protected BiFunction<V, T, Boolean> applyFunction() {
        throw new UnsupportedOperationException(
                "Override this method when you make an AbstractUpdater that uses the SetEntryProcessorOperation.APPLY operation" );
    }

}
