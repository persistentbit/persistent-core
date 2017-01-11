package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
@FunctionalInterface
public interface LogPrinter {

    PrintableText asPrintable(LogEntry logEntry);

    default <T extends LogEntry> LogPrinter orIf(Class<T> cls, SpecificLogPrinter<T> ep) {

        return new LogPrinterIfEntry(this, PMap.empty()).orIf(cls,ep);
    }

    default void print(LogEntry e) {
        System.out.println(asPrintable(e).printToString());
    }

    class LogPrinterIfEntry implements LogPrinter {
        private final LogPrinter master;
        private final PMap<Class, SpecificLogPrinter> ifPrinters;

        public LogPrinterIfEntry(LogPrinter master, PMap<Class, SpecificLogPrinter> ifPrinters) {
            this.master = master;
            this.ifPrinters = ifPrinters;
        }

        @Override
        public String toString() {
            return "LogPrinterIfEntry[" + ifPrinters.keys().map(Class::getSimpleName).toString(", ") + "]";
        }

        @Override
        public <T extends LogEntry> LogPrinter orIf(Class<T> cls, SpecificLogPrinter<T> ep) {
            return new LogPrinterIfEntry(this, ifPrinters.put(cls, ep));
        }


        @Override
        public PrintableText asPrintable(LogEntry logEntry) {
            return ifPrinters.getOpt(logEntry.getClass())
                    .map(sp -> sp.asPrintable(logEntry, this))
                    .orElse(master.asPrintable(logEntry));
        }


    }

}
