package com.persistentbit.core.logging;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryMessage implements LogEntry{
	private final LogContext source;
	private final String     message;

	private LogEntryMessage(LogContext source, String message) {
		this.source = source;
		this.message = message;
	}

	public static LogEntryMessage of(LogContext source, String message){
		return new LogEntryMessage(source,message);
	}

	@Override
	public LogEntryGroup append(LogEntry other) {
		return LogEntryGroup.empty().append(this).append(other);
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.ofNullable(source);
	}

	public String getMessage() {
		return message;
	}
}
