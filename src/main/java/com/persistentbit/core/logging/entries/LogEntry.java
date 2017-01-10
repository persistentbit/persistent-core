package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.printing.PrintableText;

import java.io.Serializable;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public interface LogEntry extends Serializable{
	LogEntry append(LogEntry other);
	Optional<LogContext> getContext();
	default boolean isEmpty() { return false; }

	PrintableText asPrintable(boolean color);

	default String printString(boolean color){
		return asPrintable(color).printToString();
	}



}
