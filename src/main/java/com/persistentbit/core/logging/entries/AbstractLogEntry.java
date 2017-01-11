package com.persistentbit.core.logging.entries;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrinter;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/01/2017
 */
public abstract class AbstractLogEntry implements LogEntry{
    static private LogPrinter toStringPrinter = ModuleCore.createLogPrinter(false);

    @Override
    public String toString() {
        return toStringPrinter.asPrintable(this).printToString();
    }


}
