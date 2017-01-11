package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
@FunctionalInterface
public interface LogPrinter{

	PrintableText asPrintable(LogEntry logEntry);

	default <T extends LogEntry> LogPrinter orIf(Class<T> cls, SpecificLogPrinter<T> ep) {
		return (logEntry) -> {
			if(cls.isAssignableFrom(logEntry.getClass())) {
				return ep.asPrintable((T) logEntry, this);
			}
			return asPrintable(logEntry);
		};
	}


}
