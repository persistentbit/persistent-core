package com.persistentbit.core.logging;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * Logging Write Monad
 *
 * @author petermuys
 * @since 13/12/16
 */
public class Logged<V> extends BaseValueClass{

	/*private final V        value;
	private final LogEntry log;

	public Logged(V value, LogEntry log) {
		this.value = value;
		this.log = log;
	}

	public V get() {
		return value;
	}

	public LogEntry getLog() {
		return log;
	}

	public <U> Logged<U> map(Function<V, U> mapper) {
		return new Logged<>(mapper.apply(value), log);
	}

	public <U> Logged<U> flatMap(Function<V, Logged<U>> mapper) {
		Logged<U> newLogged = mapper.apply(value);
		return new Logged<>(newLogged.get(), log.add(newLogged.getLog()));
	}
	*/

}
