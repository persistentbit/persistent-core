package com.persistentbit.core.function;

import com.persistentbit.core.result.Result;

import java.util.Optional;
import java.util.function.Function;

/**
 * Functional utilities
 *
 * @author petermuys
 * @since 26/12/16
 */
public interface F {


    static <T, U, V> Function<V, U> compose(Function<? super T, ? extends U> f, Function<? super V, ? extends T> g) {
        return x -> f.apply(g.apply(x));
    }

    static <T, U, V> Function<T, V> andThen(Function<? super T, ? extends U> f, Function<? super U, ? extends V> g) {
        return x -> g.apply(f.apply(x));
    }

    static <T, R> Function<T, Optional<R>> toOptional(Function<T, R> f) {
        return x -> {
            try {
                return Optional.ofNullable(f.apply(x));
            } catch (Exception e) {
                return Optional.empty();
            }
        };

    }

    static <A, B, R> Function<A, Function<B, Optional<R>>> higherToOptional(Function<A, Function<B, R>> f) {
        return x -> y -> {
            try {
                return Optional.ofNullable(f.apply(x).apply(y));
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    static <A, R> Function<A, Result<R>> toResult(Function<A, R> f) {
        return Result.toResult(f);
    }

    static <A, B, R> Function<A, Function<B, Result<R>>> higherToResult(Function<A, Function<B, R>> f) {
        return Result.higherToResult(f);
    }

    /**
     * Compose higher order functions.<br>
     * <p>
     * Use:
     * <pre>{@code
     * Function<Double,String> dbl2Str = x -> "" + x;
     * Function<Integer,Double> int2Dbl = x -> (double)x;
     *
     * static void main(String...args){
     *   String xs = F.<Integer,Double,String>higherCompose().apply(dbl2Str).apply(int2Dbl).apply(3);
     *   System.out.println(xs);  //prints string 3.0
     * }
     * }</pre>
     *
     * @param <T> argument type
     * @param <U> first type
     * @param <V> second type
     * @return uv o tu (t)
     */
    static <T, U, V> Function<Function<U, V>, Function<Function<T, U>, Function<T, V>>> higherCompose() {
        return uvFunct -> tuFunct -> t -> uvFunct.apply(tuFunct.apply(t));
    }

    static <T, U, V> Function<Function<T, U>, Function<Function<U, V>, Function<T, V>>> higherAndThen() {
        return tuFunct -> uvFunct -> t -> uvFunct.apply(tuFunct.apply(t));
    }


}
