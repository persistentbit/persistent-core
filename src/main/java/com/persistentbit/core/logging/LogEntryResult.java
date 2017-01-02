package com.persistentbit.core.logging;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/01/17
 */
public class LogEntryResult implements LogEntry{
	private LogContext	context;
	private String value;

	public LogEntryResult(LogContext context, String value) {
		this.context = context;
		this.value = value;
	}

	@Override
	public LogEntryGroup append(LogEntry other) {
		return LogEntryGroup.empty().append(this).append(other);
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.ofNullable(context);
	}
}
