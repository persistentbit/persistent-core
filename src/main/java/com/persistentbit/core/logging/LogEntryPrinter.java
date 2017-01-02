package com.persistentbit.core.logging;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public interface LogEntryPrinter{

	//void print(LogEntry entry);
	void print(Throwable exception);
}
