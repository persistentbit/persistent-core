package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * Printer for {@link LogEntry} values
 *
 * @author petermuys
 * @since 30/12/16
 */

public final class LogPrinter {
    private POrderedMap<Class, SpecificExceptionPrinter> exceptionPrinters;
    private POrderedMap<Class, SpecificLogPrinter>          logPrinters;

    private LogPrinter(POrderedMap<Class, SpecificExceptionPrinter> exceptionPrinters, POrderedMap<Class, SpecificLogPrinter> logPrinters) {
        this.exceptionPrinters = exceptionPrinters;
        this.logPrinters = logPrinters;
    }
    private LogPrinter() {
        this(POrderedMap.empty(),POrderedMap.empty());
    }
    public static LogPrinter create() {
        return new LogPrinter();
    }


    public <T extends LogEntry> LogPrinter logIf(Class<T> logEntry, SpecificLogPrinter<T> specificLogPrinter){
        return new LogPrinter(exceptionPrinters,logPrinters.put(logEntry,specificLogPrinter));
    }

    public <T extends Throwable> LogPrinter logIf(Class<T> exception, SpecificExceptionPrinter<T> specificExceptionPrinter){
        return new LogPrinter(exceptionPrinters.put(exception, specificExceptionPrinter),logPrinters);
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
    public void print(LogEntry logEntry){
        System.out.println(printableLog(logEntry).printToString());
    }
    public void print(Throwable exception){
        System.out.println(printableException(exception).printToString());
    }

    public LogPrinter registerAsGlobalHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> print(exception));
        return this;
    }
}
