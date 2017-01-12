package com.persistentbit.core.logging.cleaning;

import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.logging.entries.LogEntry;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 12/01/2017
 */

public final class LogCleaner{
    private POrderedMap<Class,SpecificLogCleaner> cleaners;

    private LogCleaner(POrderedMap<Class, SpecificLogCleaner> cleaners) {
        this.cleaners = cleaners;
    }


    public static LogCleaner create() {
        return new LogCleaner(POrderedMap.empty());
    }

    @SuppressWarnings("unchecked")
    public Optional<LogEntry> clean(LogEntry logEntry){
        return cleaners.getOpt(logEntry.getClass())
                .map(slc -> slc.clean(this,logEntry))
                .orElseGet(()->
                        cleaners.find(t -> t._1.isAssignableFrom(logEntry.getClass()))
                                .map(t -> t._2.clean(this, logEntry))
                                .orElse(Optional.of(logEntry))
                );
    }

    public <L extends LogEntry> LogCleaner orIf(Class<L> cls, SpecificLogCleaner<L> cleaner){
        return new LogCleaner(cleaners.put(cls, cleaner));
    }
}
