package com.dataloom.hazelcast;

import com.google.common.util.concurrent.ListenableFuture;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.ICompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ListenableHazelcastFuture<T> implements ListenableFuture<T> {
    private static final Logger logger = LoggerFactory.getLogger( ListenableHazelcastFuture.class );
    private final ICompletableFuture<T> f;

    public ListenableHazelcastFuture( ICompletableFuture<T> f ) {
        this.f = f;
    }

    @Override
    public void addListener( Runnable listener, Executor executor ) {
        f.andThen( new ExecutionCallback<T>() {
            @Override public void onResponse( T response ) {
                listener.run();
            }

            @Override public void onFailure( Throwable t ) {
                logger.error( "Unable to retrieve Ace.", t );
            }
        } );
    }

    @Override
    public boolean cancel( boolean mayInterruptIfRunning ) {
        return f.cancel( mayInterruptIfRunning );
    }

    @Override public boolean isCancelled() {
        return f.isCancelled();
    }

    @Override
    public boolean isDone() {
        return f.isDone();
    }

    @Override public T get() throws InterruptedException, ExecutionException {
        return f.get();
    }

    @Override public T get( long timeout, TimeUnit unit )
            throws InterruptedException, ExecutionException, TimeoutException {
        return f.get( timeout, unit );
    }
}
