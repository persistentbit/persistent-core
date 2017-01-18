package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public interface LogOut {

    void print(LogEntry logEntry);
    void print(Throwable exception);

    default LogOut add(LogOut other){
        return new LogOutGroup().add(this).add(other);
    }

    default LogOut registerAsGlobalHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> print(exception));
        return this;
    }
}
