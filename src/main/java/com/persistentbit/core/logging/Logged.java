package com.persistentbit.core.logging;

import java.util.List;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/12/16
 */
public class Logged<V>{
	private final List<LogEntry> logs;
	private final V value;

	public Logged(List<LogEntry> logs, V value) {
		this.logs = logs;
		this.value = value;
	}

	public List<LogEntry> getLogs() {
		return logs;
	}

	public V getValue() {
		return value;
	}
}
