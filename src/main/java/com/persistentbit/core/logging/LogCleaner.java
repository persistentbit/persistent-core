package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 6/01/2017
 */
public class LogCleaner {
    public static LogEntry clean(LogEntry logEntry){
        if(logEntry instanceof LogEntryEmpty){
            return clean((LogEntryEmpty)logEntry);
        } else if(logEntry instanceof LogEntryFunction){
            return clean((LogEntryFunction)logEntry);
        } else if(logEntry instanceof LogEntryException){
            return clean((LogEntryException)logEntry);
        } else if(logEntry instanceof LogEntryGroup){
            return clean((LogEntryGroup)logEntry);
        } else if(logEntry instanceof LogEntryMessage){
            return clean((LogEntryMessage)logEntry);
        } else{
            throw new RuntimeException("Unknown log entry:" + logEntry);
        }

    }
    public static LogEntry clean(LogEntryFunction le){
        LogEntry cleaned = clean(le.getLogs());
        if(cleaned.isEmpty()){
            return LogEntryEmpty.inst;
        }
        return le.withLogs(cleaned);
    }
    public static LogEntry clean(LogEntryEmpty le){
        return le;
    }
    public static LogEntry clean(LogEntryException le){
        return le;
    }
    public static LogEntry clean(LogEntryGroup le) {
        return le.getEntries().fold(LogEntryEmpty.inst,(a,b)-> a.append(clean(b)));
    }
    public static LogEntry clean(LogEntryMessage le){
        switch (le.getLevel()) {
            case error:
            case important:
            case warning:
                return le;
            case info:
                return LogEntryEmpty.inst;
                default:
                    return le;
        }
    }

}
