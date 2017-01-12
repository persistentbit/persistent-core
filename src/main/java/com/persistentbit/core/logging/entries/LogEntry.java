package com.persistentbit.core.logging.entries;

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

}
