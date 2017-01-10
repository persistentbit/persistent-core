package com.persistentbit.core.logging.entries;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.printing.PrintableText;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryGroup implements LogEntry{
	private final PList<LogEntry> entries;

	private LogEntryGroup(PList<LogEntry> entries) {
		this.entries = entries;
	}

	@Override
	public String toString() {
		return entries.toString(",");
	}

	public static LogEntryGroup empty(){
		return new LogEntryGroup(PList.empty());
	}

	@Override
	public LogEntryGroup append(LogEntry other) {
		return other.isEmpty() ? this : new LogEntryGroup(this.entries.plus(other));
	}

	@Override
	public Optional<LogContext> getContext() {
		return entries.isEmpty()
			? Optional.empty()
			: entries.headOpt().flatMap(LogEntry::getContext);
	}

	public PList<LogEntry> getEntries() {
		return entries;
	}

	@Override
	public PrintableText asPrintable(boolean color) {
		throw new RuntimeException("LogEntryGroup.asPrintable TODO: Not yet implemented");
	}
}
