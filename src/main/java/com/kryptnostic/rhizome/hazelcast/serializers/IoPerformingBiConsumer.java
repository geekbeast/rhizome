package com.kryptnostic.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Objects;

@FunctionalInterface
public interface IoPerformingBiConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void accept( T t, U u ) throws IOException;

    /**
     * Returns a composed {@code BiConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code BiConsumer} that performs in sequence this operation followed by the {@code after}
     *         operation
     * @throws NullPointerException if {@code after} is null
     */
    default IoPerformingBiConsumer<T, U> andThen( IoPerformingBiConsumer<? super T, ? super U> after )
            throws IOException {
        Objects.requireNonNull( after );

        return ( l, r ) -> {
            accept( l, r );
            after.accept( l, r );
        };
    }
}
