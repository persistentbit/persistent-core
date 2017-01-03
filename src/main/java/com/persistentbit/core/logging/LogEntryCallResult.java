package com.persistentbit.core.logging;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/01/17
 */
public class LogEntryCallResult implements LogEntry{
	private final LogContext     context;
	private final LogResultLevel level;
	private final String         value;

	public LogEntryCallResult(LogContext context, LogResultLevel level, String value) {
		this.context = context;
		this.level = level;
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

	public LogResultLevel getLevel() {
		return level;
	}

	public String getValue() {
		return value;
	}
}
