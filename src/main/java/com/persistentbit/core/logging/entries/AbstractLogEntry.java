package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/01/2017
 */
public abstract class AbstractLogEntry implements LogEntry{


    @Override
    public String toString() {
        return printString(false);
    }

    @Override
    public PrintableText asPrintable(boolean color) {
        return asPrintable(color ? LogEntryDefaultFormatting.colors : LogEntryDefaultFormatting.noColors);
    }
    protected abstract PrintableText asPrintable(LogEntryDefaultFormatting formatting);
}
