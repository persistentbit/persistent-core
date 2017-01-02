package com.persistentbit.core.logging;

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


}
