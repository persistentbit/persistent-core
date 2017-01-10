package com.persistentbit.core.logging.printing;

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
public class LogEntryDefaultFormatting implements LogEntryFormatting{
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM hh:mm:ss.SSS");

    public String formatTime(long time) {
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }
    public String styleException(String exception){

    }
    public String styleInfo(String info){

    }
    public String styleClassName(String className){

    }

}
