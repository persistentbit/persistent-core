package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 13/12/2016
 */
public interface LogEntry {

    boolean hasError();

	void accept(LogEntryVisitor visitor);


}
