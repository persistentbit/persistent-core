package com.persistentbit.core;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Lazy evaluated value that is automatically refreshed after a specified time since the last evaluation.
 *
 * @author petermuys
 * @since 28/01/17
 */
public class Cached<T> implements Supplier<T>{

	private final Supplier<T> supplier;
	private final Duration    refreshTime;
	private       T           value;
	private       boolean     gotValue;
	private       Instant     valueTime;

	/**
	 * Init with the master supplier
	 *
	 * @param refreshTime Duration that a valid is cached
	 * @param supplier    The master supplier
	 */
	public Cached(Duration refreshTime, Supplier<T> supplier) {
		this.supplier = supplier;
		this.refreshTime = refreshTime;
	}

	/**
	 * Get the value from the master supplier on the first call or on refresh timeout.<br>
	 * Then reuse the value in all the later calls until the refresh timeout.<br>
	 *
	 * @return The lazy value
	 */
	@Override
	public synchronized T get() {
		gotValue = gotValue && valueTime.isBefore(Instant.now());
		if(gotValue == false) {
			value = supplier.get();
			valueTime = Instant.now().plus(refreshTime);
			gotValue = true;
		}
		return value;
	}
}