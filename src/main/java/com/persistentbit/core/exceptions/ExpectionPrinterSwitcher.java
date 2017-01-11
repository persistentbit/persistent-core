package com.persistentbit.core.exceptions;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.printing.PrintableText;

/**
 * Specific exception printer switcher.
 *
 * @author Peter Muys
 * @since 11/01/2017
 */
final class ExpectionPrinterSwitcher implements ExceptionPrinter{
    private ExceptionPrinter master;
    private PMap<Class,SpecificExceptionPrinter> specificPrinter;

    public ExpectionPrinterSwitcher(ExceptionPrinter master, PMap<Class, SpecificExceptionPrinter> specificPrinter) {
        this.master = master;
        this.specificPrinter = specificPrinter;
    }

    public ExpectionPrinterSwitcher(ExceptionPrinter master) {
        this(master,PMap.empty());
    }
    @Override
    public String toString() {
        return "ExpectionPrinterSwitcher[" + specificPrinter.keys().map(Class::getSimpleName).toString(", ") + "]";
    }

    @Override
    public <T extends Throwable> ExceptionPrinter orIf(Class<T> cls, SpecificExceptionPrinter<T> ep) {
        return new ExpectionPrinterSwitcher(this, specificPrinter.put(cls, ep));
    }


    @Override
    public PrintableText asPrintable(Throwable logEntry) {
        return specificPrinter.getOpt(logEntry.getClass())
                .map(sp -> sp.asPrintable(logEntry, this))
                .orElse(master.asPrintable(logEntry));
    }

}
