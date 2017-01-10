package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 31/12/16
 */
public class LogEntryException implements LogEntry{
	private final LogContext source;
	private final Throwable cause;

	public LogEntryException(LogContext source, Throwable cause) {
		this.source = source;
		this.cause = cause;
	}

	public LogEntryException(Throwable cause){
		this(cause.getStackTrace() == null ? null : new LogContext(cause.getStackTrace()[0]),cause);
	}

	@Override
	public LogEntry append(LogEntry other) {
		return this;
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.ofNullable(source);
	}

	public Throwable getCause() {
		return cause;
	}
}
