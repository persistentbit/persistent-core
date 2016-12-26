package com.persistentbit.core.function;

import java.util.function.Function;

/**
 * Functional utilities
 *
 * @author petermuys
 * @since 26/12/16
 */
@FunctionalInterface
public interface F<T, U>{

	U apply(T arg);

	default <V> F<V, U> compose(F<? super V, ? extends T> f) {
		return x -> this.apply(f.apply(x));
	}

	default <V> F<T, V> andThen(F<? super U, ? extends V> f) {
		return x -> f.apply(this.apply(x));
	}

	static <T> F<T, T> identity() {
		return t -> t;
	}

	static <T, U, V> F<V, U> compose(F<? super T, ? extends U> f, F<? super V, ? extends T> g) {
		return x -> f.apply(g.apply(x));
	}

	static <T, U, V> F<T, V> andThen(F<? super T, ? extends U> f, F<? super U, ? extends V> g) {
		return x -> g.apply(f.apply(x));
	}

	static <T, U, V> F<F<T, U>, F<F<U, V>, F<T, V>>> compose() {
		return x -> y -> y.compose(x);
	}

	static <T, U, V> F<F<T, U>, F<F<V, T>, F<V, U>>> andThen() {
		return x -> y -> y.andThen(x);
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
	 *
	 * @return uv o tu (t)
	 */
	static <T, U, V> Function<Function<U, V>, Function<Function<T, U>, Function<T, V>>> higherCompose() {
		return uvFunct -> tuFunct -> t -> uvFunct.apply(tuFunct.apply(t));
	}

	static <T, U, V> Function<Function<T, U>, Function<Function<U, V>, Function<T, V>>> higherAndThen() {
		return tuFunct -> uvFunct -> t -> uvFunct.apply(tuFunct.apply(t));
	}


}
