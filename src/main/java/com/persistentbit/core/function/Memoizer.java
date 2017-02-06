package com.persistentbit.core.function;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.TimeMeasurement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Wrapper for a function that remembers the calculated value for each parameter.<br>
 * example: {@code F<A,B> cached = Memoizer.of(functionAToB) }
 *
 * @author petermuys
 * @since 26/12/16
 */

public class Memoizer<T, R> implements Function<T, R>{

	private final Map<T, R> cache = new ConcurrentHashMap();
	private final Function<T, R> f;

	private Memoizer(Function<T, R> f) {
		this.f = f;
	}

	public R apply(T value) {
		return cache.computeIfAbsent(value, f);
	}

	public static <T, R> Function<T, R> of(Function<T, R> f) {
		return new Memoizer<>(f);
	}

	public static void main(String[] args) {
		Function<Integer, Function<Integer, Integer>> slowCalc = a -> b -> {
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				throw new RuntimeException("TODO ERROR HANDLING", e);
			}
			return a + b;
		};
		Function<Integer, Function<Integer, Integer>> cachedCalc =
			Memoizer.of(a -> Memoizer.of(b -> slowCalc.apply(a).apply(b)));

		PStream<Integer> range = PStream.sequence(0).limit(10);
		TimeMeasurement.runAndLog(() ->
									  System.out.println(range.fold(0, cachedCalc))
		);
		TimeMeasurement.runAndLog(() ->
									  System.out.println(range.fold(0, cachedCalc))
		);

	}
}
