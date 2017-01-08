package com.persistentbit.core.logging;

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
