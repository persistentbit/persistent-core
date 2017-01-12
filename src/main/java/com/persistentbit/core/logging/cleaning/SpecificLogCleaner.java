package com.persistentbit.core.logging.cleaning;

import com.persistentbit.core.logging.entries.LogEntry;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 12/01/2017
 */
@FunctionalInterface
public interface SpecificLogCleaner <T extends LogEntry> {

    Optional<LogEntry> clean(LogCleaner rootCleaner, T logEntry);

    default <L extends T> SpecificLogCleaner<T> orIf(Class<L> cls, SpecificLogCleaner<L> logCleaner) {
        SpecificLogCleaner<T> self = this;
        return (rootCleaner, logEntry) -> {
            if (cls.isAssignableFrom(logEntry.getClass())) {
                return logCleaner.clean(rootCleaner, (L) logEntry);
            }
            return self.clean(rootCleaner, logEntry);
        };
    }
}