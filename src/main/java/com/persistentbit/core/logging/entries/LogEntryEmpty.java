package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.printing.PrintableText;

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


	@Override
	protected PrintableText asPrintable(LogEntryDefaultFormatting formatting) {
		return PrintableText.empty;
	}
}
