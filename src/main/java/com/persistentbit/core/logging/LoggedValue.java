package com.persistentbit.core.logging;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/01/17
 */
public interface LoggedValue<THIS extends LoggedValue>{
	LogEntry getLog();
	THIS mapLog(Function<LogEntry,LogEntry> f);
}
