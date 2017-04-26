package com.persistentbit.core.logging.entries;

import com.persistentbit.core.doc.annotations.DComposite;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryMessage extends AbstractLogEntry{
	@DComposite
	private final LogMessageLevel level;
	private final LogContext source;
	private final String     message;

	public LogEntryMessage(LogMessageLevel level, LogContext source, String message) {
		this.level = level;
		this.source = source;
		this.message = message;
	}


	public static LogEntryMessage of(LogMessageLevel level, LogContext source, String message){
		return new LogEntryMessage(level, source,message);
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

	public LogMessageLevel getLevel() {
		return level;
	}



}
