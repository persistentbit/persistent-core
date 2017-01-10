package com.persistentbit.core.logging;

import com.persistentbit.core.logging.entries.LogEntry;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public interface LogEntryPrinter{

	void print(LogEntry entry);
	void print(Throwable exception);
	default <E extends LoggedValue> E print(E lv){
		print(lv.getLog());
		return lv;
	}


	default LogEntryPrinter registerAsGlobalHandler() {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> print(e));
		return this;
	}
}
