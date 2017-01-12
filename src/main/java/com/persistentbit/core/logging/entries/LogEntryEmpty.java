package com.persistentbit.core.logging.entries;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/01/17
 */
public class LogEntryEmpty extends AbstractLogEntry{
	public static final LogEntryEmpty inst = new LogEntryEmpty();

	private LogEntryEmpty(){

	}

	@Override
	public LogEntry append(LogEntry other) {
		return other;
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.empty();
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
