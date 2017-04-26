package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.doc.annotations.DAggregateOf;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * Printer for {@link LogEntry} values
 *
 * @author petermuys
 * @since 30/12/16
 */
@DAggregateOf(value = SpecificExceptionPrinter.class,otherLabel = "exceptionPrinters")
@DAggregateOf(value = SpecificLogPrinter.class,otherLabel = "logPrinters")
public final class LogFormatter{
    private POrderedMap<Class, SpecificExceptionPrinter> exceptionPrinters;
    private POrderedMap<Class, SpecificLogPrinter>          logPrinters;

    private LogFormatter(POrderedMap<Class, SpecificExceptionPrinter> exceptionPrinters,
                         POrderedMap<Class, SpecificLogPrinter> logPrinters
    ) {
        this.exceptionPrinters = exceptionPrinters;
        this.logPrinters = logPrinters;
    }

    private LogFormatter() {
        this(POrderedMap.empty(),POrderedMap.empty());
    }

    public static LogFormatter create() {
        return new LogFormatter();
    }


    public <T extends LogEntry> LogFormatter logIf(Class<T> logEntry, SpecificLogPrinter<T> specificLogPrinter) {
        return new LogFormatter(exceptionPrinters, logPrinters.put(logEntry, specificLogPrinter));
    }

    public <T extends Throwable> LogFormatter logIf(Class<T> exception,
                                                    SpecificExceptionPrinter<T> specificExceptionPrinter
    ) {
        return new LogFormatter(exceptionPrinters.put(exception, specificExceptionPrinter), logPrinters);
    }

    public PrintableText    printableLog(LogEntry logEntry){
        if(logEntry == null){
            return null;
        }
        return logPrinters.getOpt(logEntry.getClass())
                .map(slp -> slp.asPrintable(logEntry,this))
                .orElseGet(()->
                    logPrinters.find(t -> t._1.isAssignableFrom(logEntry.getClass()))
                            .map(t -> t._2.asPrintable(logEntry,this))
                            .orElse(PrintableText.from(logEntry))
                );

    }

    public PrintableText printableException(Throwable exception){

        return exceptionPrinters.getOpt(exception.getClass())
                .map(slp -> slp.asPrintable(exception,this))
                .orElseGet(()->
                        exceptionPrinters.find(t -> t._1.isAssignableFrom(exception.getClass()))
                                .map(t -> t._2.asPrintable(exception,this))
                                .orElse(PrintableText.from(exception))
                );
    }

}
