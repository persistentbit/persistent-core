package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.TimeMeasurement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * TODOC
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

	static <T, R> Function<T, R> of(Function<T, R> f) {
		return new Memoizer<>(f);
	}

	public static void main(String[] args) {
		Function<Integer, Function<Integer, Integer>> add = Memoizer.of(a ->
															  Memoizer.of(b -> {
																  try {
																	  Thread.sleep(500);
																  } catch(InterruptedException e) {
																	  throw new RuntimeException("TODO ERROR HANDLING", e);
																  }
																  return a + b;
															  }));

		PStream<Integer> range = PStream.sequence(0).limit(10);
		TimeMeasurement.runAndLog(() ->
									  System.out.println(range.fold(0, add))
		);
		TimeMeasurement.runAndLog(() ->
									  System.out.println(range.fold(0, add))
		);

	}
}
