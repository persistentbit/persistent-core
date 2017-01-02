package com.persistentbit.core.logging;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/01/17
 */
public interface LoggedValue<T extends LoggedValue>{
	Optional<LogEntry> getLog();
	T withLogs(LogEntry le);
}
