package com.persistentbit.core.logging.entries;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.printing.PrintableText;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryGroup extends AbstractLogEntry{
	private final PList<LogEntry> entries;

	private LogEntryGroup(PList<LogEntry> entries) {
		this.entries = entries;
	}



	public static LogEntryGroup empty() {
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
	protected PrintableText asPrintable(LogEntryDefaultFormatting formatting) {
		return out -> entries.forEach(le -> out.print(le.asPrintable(formatting.hasColor)));
	}
}
