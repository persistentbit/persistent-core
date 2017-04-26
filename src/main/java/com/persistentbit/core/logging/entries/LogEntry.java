package com.persistentbit.core.logging.entries;

import com.persistentbit.core.doc.annotations.DAggregateOf;

import java.io.Serializable;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
@DAggregateOf(value = LogContext.class,label = "context",thisLabel = "1",otherLabel = "1")
public interface LogEntry extends Serializable{
	LogEntry append(LogEntry other);
	Optional<LogContext> getContext();
	default boolean isEmpty() { return false; }

}
