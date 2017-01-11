package com.persistentbit.core;

import com.persistentbit.core.exceptions.DefaultExceptionPrinter;
import com.persistentbit.core.exceptions.ExceptionPrinter;
import com.persistentbit.core.logging.LoggedException;
import com.persistentbit.core.logging.entries.*;
import com.persistentbit.core.logging.printing.DefaultLogPrinter;
import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.logging.printing.LogPrinter;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/01/2017
 */
public final class ModuleCore {
    public static LogPrinter createLogPrinter(boolean hasColor){
        LogEntryDefaultFormatting format = hasColor
                ? LogEntryDefaultFormatting.colors
                : LogEntryDefaultFormatting.noColors;

        return DefaultLogPrinter.forLogEntry()
                .orIf(LogEntryGroup.class, DefaultLogPrinter.forLogEntryGroup(format))
                .orIf(LogEntryEmpty.class, DefaultLogPrinter.forLogEntryEmpty(format))
                .orIf(LogEntryException.class, DefaultLogPrinter.forLogEntryException(format))
                .orIf(LogEntryFunction.class, DefaultLogPrinter.forLogEntryFunction(format))
                .orIf(LogEntryMessage.class, DefaultLogPrinter.forLogEntryMessage(format))
                ;
    }
    public static ExceptionPrinter createExceptionPrinter(LogPrinter logPrinter, boolean color) {
        LogEntryDefaultFormatting format =
                color ? LogEntryDefaultFormatting.colors : LogEntryDefaultFormatting.noColors;

        return new DefaultExceptionPrinter(format)
                .orIf(LoggedException.class,LoggedException.createExceptionPrinter(logPrinter,color));
    }
    public static ExceptionPrinter createExceptionPrinter(boolean color) {
        LogEntryDefaultFormatting format =
                color ? LogEntryDefaultFormatting.colors : LogEntryDefaultFormatting.noColors;

        return new DefaultExceptionPrinter(format)
                .orIf(LoggedException.class,LoggedException.createExceptionPrinter(createLogPrinter(color),color));
    }
}
