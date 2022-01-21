package com.geekbeast.rhizome.hazelcast.serializers;

import java.io.IOException;
import java.util.Objects;
/**
 * Represents a function that performs I/O while accepting one argument and producing a result. 
 * 
 * It may throw an IOException.
 *
 * <p>This is a <a href="package-summary.html">IoPerformingFunctional interface</a>
 * whose IoPerformingFunctional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the IoPerformingFunction
 * @param <R> the type of the result of the IoPerformingFunction
 *
 */

@FunctionalInterface
public interface IoPerformingFunction<T, R> {

    /**
     * Applies this IoPerformingFunction to the given argument.
     *
     * @param t the IoPerformingFunction argument
     * @return the IoPerformingFunction result
     */
    R apply(T t) throws IOException;

    /**
     * Returns a composed IoPerformingFunction that first applies the {@code before}
     * IoPerformingFunction to its input, and then applies this IoPerformingFunction to the result.
     * If evaluation of either IoPerformingFunction throws an exception, it is relayed to
     * the caller of the composed IoPerformingFunction.
     *
     * @param <V> the type of input to the {@code before} IoPerformingFunction, and to the
     *           composed IoPerformingFunction
     * @param before the IoPerformingFunction to apply before this IoPerformingFunction is applied
     * @return a composed IoPerformingFunction that first applies the {@code before}
     * IoPerformingFunction and then applies this IoPerformingFunction
     * @throws NullPointerException if before is null
     *
     * @see #andThen(IoPerformingFunction)
     */
    default <V> IoPerformingFunction<V, R> compose(IoPerformingFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed IoPerformingFunction that first applies this IoPerformingFunction to
     * its input, and then applies the {@code after} IoPerformingFunction to the result.
     * If evaluation of either IoPerformingFunction throws an exception, it is relayed to
     * the caller of the composed IoPerformingFunction.
     *
     * @param <V> the type of output of the {@code after} IoPerformingFunction, and of the
     *           composed IoPerformingFunction
     * @param after the IoPerformingFunction to apply after this IoPerformingFunction is applied
     * @return a composed IoPerformingFunction that first applies this IoPerformingFunction and then
     * applies the {@code after} IoPerformingFunction
     * @throws NullPointerException if after is null
     *
     * @see #compose(IoPerformingFunction)
     */
    default <V> IoPerformingFunction<T, V> andThen(IoPerformingFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Returns a IoPerformingFunction that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the IoPerformingFunction
     * @return a IoPerformingFunction that always returns its input argument
     */
    static <T> IoPerformingFunction<T, T> identity() {
        return t -> t;
    }
}
