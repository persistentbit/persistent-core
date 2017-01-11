package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

/**
 * Log Printer that can switch different Specific printers
 *
 * @author Peter Muys
 * @since 11/01/2017
 */
class LogPrinterSwitcher implements LogPrinter {
    private final LogPrinter master;
    private final PMap<Class, SpecificLogPrinter> ifPrinters;

    public LogPrinterSwitcher(LogPrinter master, PMap<Class, SpecificLogPrinter> ifPrinters) {
        this.master = master;
        this.ifPrinters = ifPrinters;
    }

    @Override
    public String toString() {
        return "LogPrinterIfEntry[" + ifPrinters.keys().map(Class::getSimpleName).toString(", ") + "]";
    }

    @Override
    public <T extends LogEntry> LogPrinter orIf(Class<T> cls, SpecificLogPrinter<T> ep) {
        return new LogPrinterSwitcher(this, ifPrinters.put(cls, ep));
    }


    @Override
    public PrintableText asPrintable(LogEntry logEntry) {
        return ifPrinters.getOpt(logEntry.getClass())
                .map(sp -> sp.asPrintable(logEntry, this))
                .orElse(master.asPrintable(logEntry));
    }


}