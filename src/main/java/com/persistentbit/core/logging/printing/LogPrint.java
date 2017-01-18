package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;

/**
 * A LogPrint instance is responsible for writing out Logging data.<br>
 * This will most likely be done using a {@link LogFormatter}
 *
 * @author Peter Muys
 * @since 18/01/2017
 * @see LogFormatter
 * @see LogPrintStream
 * @see LogPrintToFile
 */
public interface LogPrint{

    void print(LogEntry logEntry);
    void print(Throwable exception);

    default LogPrint add(LogPrint other) {
        return new LogPrintGroup().add(this).add(other);
    }

    default LogPrint registerAsGlobalHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> print(exception));
        return this;
    }
}
