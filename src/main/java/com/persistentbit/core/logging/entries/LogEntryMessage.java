package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.logging.LogMessageLevel;
import com.persistentbit.core.printing.PrintableText;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryMessage implements LogEntry{
	private final LogMessageLevel level;
	private final LogContext source;
	private final String     message;

	private LogEntryMessage(LogMessageLevel level, LogContext source, String message) {
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

	@Override
	public PrintableText asPrintable(boolean color) {
		return out -> {
			out.println(
					msgStyleDebug +  entry.getMessage() +

							timeStyle + "\t… " + entry.getContext().map(s -> formatTime(s.getTimestamp()) + " ").orElse("") +
							classStyle  +  entry.getContext().map(s -> s.getClassName() + "(" + s.getFileName() + ":" + s.getSourceLine() + ")").orElse("")
			);
		}
	}
}
