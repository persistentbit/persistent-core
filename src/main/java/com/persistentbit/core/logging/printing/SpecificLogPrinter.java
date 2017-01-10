package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/01/17
 */
@FunctionalInterface
public interface SpecificLogPrinter<E extends LogEntry>{

	PrintableText asPrintable(E logEntry, LogPrinter rootPrinter);


	default <T extends E> SpecificLogPrinter<E> orIf(Class<T> cls, SpecificLogPrinter<T> ep) {
		return (logEntry, rootPrinter) -> {
			if(cls.isAssignableFrom(logEntry.getClass())) {
				return ep.asPrintable((T) logEntry, rootPrinter);
			}
			return asPrintable(logEntry, rootPrinter);
		};
	}


}
