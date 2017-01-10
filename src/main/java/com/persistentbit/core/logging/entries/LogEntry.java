package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.logging.printing.LogEntryFormatting;
import com.persistentbit.core.printing.PrintableText;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public interface LogEntry{
	LogEntry append(LogEntry other);
	Optional<LogContext> getContext();
	default boolean isEmpty() { return false; }

	PrintableText asPrintable(LogEntryFormatting formatting);
}
