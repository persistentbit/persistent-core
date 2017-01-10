package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.AnsiColor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/01/2017
 */
public class LogEntryDefaultFormatting {
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM hh:mm:ss.SSS");
    public final String timeStyle;
    public final String classStyle;
    public final String functionStyle;
    public final String functionParamsStyle;
    public final String functionResultStyle;
    public final String durationStyle;
    public final String msgStyleDebug;
    public final String msgStyleInfo;
    public final String msgStyleWarn;
    public final String msgStyleError;
    public final String msgStyleException;
    public final boolean hasColor;
    public LogEntryDefaultFormatting(AnsiColor color) {
        hasColor = color.isActive();
        this.timeStyle = color.faint().fgWhite().toString();
        this.classStyle = color.faint().fgWhite().toString();
        this.functionStyle = color.bold().fgYellow().toString();
        this.functionParamsStyle = color.fgYellow().toString();
        this.functionResultStyle  = color.fgBlue().toString();
        this.durationStyle = color.faint().fgWhite().toString();
        this.msgStyleDebug = color.fgCyan().toString();
        this.msgStyleInfo = color.fgGreen().toString();
        this.msgStyleWarn = color.fgRed().toString();
        this.msgStyleError = color.bold().fgRed().toString();
        this.msgStyleException = color.fgRed().toString();
    }

    public boolean hasColor() {
        return hasColor;
    }

    public String formatTime(long time) {
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }


}
