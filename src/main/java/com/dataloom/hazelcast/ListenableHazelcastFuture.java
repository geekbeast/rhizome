package com.dataloom.hazelcast;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ListenableHazelcastFuture<T> implements ListenableFuture<T> {
    private static final Logger             logger = LoggerFactory.getLogger( ListenableHazelcastFuture.class );
    private              CompletionStage<T> f;

    public ListenableHazelcastFuture( CompletionStage<T> f ) {
        this.f = f;
    }

    @Override
    public void addListener( Runnable listener, Executor executor ) {
        f = f.whenCompleteAsync( ( v, t ) -> {
            if ( t == null ) {
                executor.execute( listener );
            } else {
                logger.error( "Unable to retrieve result.", t );
            }
        } );
    }

    @Override
    public boolean cancel( boolean mayInterruptIfRunning ) {
        return f.toCompletableFuture().cancel( mayInterruptIfRunning );

    }

    @Override public boolean isCancelled() {
        return f.toCompletableFuture().isCancelled();
    }

    @Override
    public boolean isDone() {
        return f.toCompletableFuture().isDone();
    }

    @Override public T get() throws InterruptedException, ExecutionException {
        return f.toCompletableFuture().get();
    }

    @Override public T get( long timeout, TimeUnit unit )
            throws InterruptedException, ExecutionException, TimeoutException {
        return f.toCompletableFuture().get( timeout, unit );
    }
}
