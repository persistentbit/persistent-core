package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * Printer for {@link LogEntry} values
 *
 * @author petermuys
 * @since 30/12/16
 */
@FunctionalInterface
public interface LogPrinter {

    PrintableText asPrintable(LogEntry logEntry);

    default <T extends LogEntry> LogPrinter orIf(Class<T> cls, SpecificLogPrinter<T> ep) {

        return new LogPrinterSwitcher(this, PMap.empty()).orIf(cls,ep);
    }

    default void print(LogEntry e) {
        System.out.println(asPrintable(e).printToString());
    }



}
