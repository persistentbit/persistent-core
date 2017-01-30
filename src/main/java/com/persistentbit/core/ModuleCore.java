package com.persistentbit.core;

import com.persistentbit.core.logging.LoggedException;
import com.persistentbit.core.logging.cleaning.LogCleaner;
import com.persistentbit.core.logging.entries.*;
import com.persistentbit.core.logging.printing.DefaultExceptionPrinter;
import com.persistentbit.core.logging.printing.DefaultLogPrinter;
import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.logging.printing.LogFormatter;

import java.util.Optional;

/**
 * Global settings for this persistent-core module.
 *
 * @author Peter Muys
 * @since 11/01/2017
 */
public final class ModuleCore {

    public static LogFormatter createLogFormatter(boolean hasColor) {
        LogEntryDefaultFormatting format = hasColor
                ? LogEntryDefaultFormatting.colors
                : LogEntryDefaultFormatting.noColors;

        return LogFormatter.create()
                //LogEntries
                .logIf(LogEntryGroup.class, DefaultLogPrinter.forLogEntryGroup(format))
                .logIf(LogEntryEmpty.class, DefaultLogPrinter.forLogEntryEmpty(format))
                .logIf(LogEntryException.class, DefaultLogPrinter.forLogEntryException(format))
                .logIf(LogEntryFunction.class, DefaultLogPrinter.forLogEntryFunction(format))
                .logIf(LogEntryMessage.class, DefaultLogPrinter.forLogEntryMessage(format))
                //Exceptions
                .logIf(LoggedException.class, LoggedException.createExceptionPrinter(format))
                .logIf(Throwable.class, new DefaultExceptionPrinter(format))
                ;
    }
    public static LogCleaner cleaner() {
        return LogCleaner.create()
                .orIf(LogEntryEmpty.class,(rc, le)-> Optional.empty())
                .orIf(LogEntryGroup.class,(LogCleaner rc, LogEntryGroup le) ->
                    le.getEntries()
                            .map(e -> rc.clean(e).orElse(null))
                            .filterNulls().isEmpty()
                                ? Optional.<LogEntry>empty()
                                : Optional.<LogEntry>of(le)
                )
                .orIf(LogEntryException.class,(rc, le) -> Optional.of(le))
                .orIf(LogEntryMessage.class, (rc, le) -> {
                    switch(le.getLevel()){
                        case error:
                        case warning:
                        case important:
                            return Optional.of(le);
                        default:
                            return Optional.empty();
                    }
                })
                .orIf(LogEntryFunction.class,(rc, le) ->
                    rc.clean(le.getLogs()).map(s -> le)
                )
        ;
    }
}
