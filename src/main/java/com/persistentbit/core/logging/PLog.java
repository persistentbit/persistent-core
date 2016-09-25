package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PStream;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logging class that internally uses{@link Logger} to do the real logging.<br>
 * Typical use:
 * <code>{@code
 *      public class MyClass{
 *          static private JLogger log = JLogger.get(MyClass.class);
 *
 *          void myFunction(){
 *              log.info("In My Function");
 *          }
 *      }
 * }</code>
 *
 */
public class PLog {
    static{
        setDefaultConsoleLogger();
    }

    private final java.util.logging.Logger impl;

    private PLog(String name){
        impl = java.util.logging.Logger.getLogger(name);
    }

    /**
     * Get the logger for the given class
     * @param cls Class to get a logger for
     * @return a JLogger.
     */
    static public PLog get(Class cls){
        return get(cls.getName());
    }
    static public PLog get(String name){
        return new PLog(name);
    }


    /**
     * Init the logging system by setting a new logging format that logs on 1 line and setting the
     * log level to FINEST
     */
    static public void setDefaultConsoleLogger() {
    /*
        1: date
            - a Date object representing event time of the log record.
        2: source
            - a string representing the caller, if available; otherwise, the logger's name.
        3: logger
            - the logger's name.
        4: level
            - the log level.
        5: message
            - the formatted log message returned from the Formatter.formatMessage(LogRecord) method. It uses java.text formatting and does not use the java.util.Formatter format argument.
        6: thrown
            - a string representing the throwable associated with the log record and its backtrace beginning with a newline character, if any; otherwise, an empty string.
    */
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s (%2$s) :  %5$s %6$s%n");

        Logger global = Logger.getLogger("");
        global.setLevel(Level.FINEST);

        //global.getHandlers();
        PStream.from(global.getHandlers()).forEach(h ->global.removeHandler(h));
        ConsoleHandler ch= new ConsoleHandler();
        ch.setLevel(Level.FINEST);
        global.addHandler(ch);

    }

    /**
     * Set the global level (name = "")
     * @param level The Level
     */
    static public void setDefaultLevel(Level level){
        setLevel("",level);
    }

    /**
     * Set the log level for the given name.
     * @param name The name of the logger
     * @param level The new Level
     */
    static public void setLevel(String name, Level level){
        Logger.getLogger(name).setLevel(level);
    }
    static public void setLevel(Class cls,Level level){
        setLevel(cls.getName(),level);
    }
    static public void setLevel(Package pack,Level level){
        setLevel(pack.getName(),level);
    }

    public void info(String msg){
        if(impl.isLoggable(Level.INFO)) {
            StackTraceElement s = Thread.currentThread().getStackTrace()[2];
            impl.logp(Level.INFO, s.getClassName(), s.getMethodName() + ":" + s.getLineNumber(), msg);
        }
    }
    public void debug(String msg){
        if(impl.isLoggable(Level.FINE)) {
            StackTraceElement s = Thread.currentThread().getStackTrace()[2];
            impl.logp(Level.FINE, s.getClassName(), s.getMethodName() + ":" + s.getLineNumber(), msg);
        }
    }

    public void trace(String msg){
        if(impl.isLoggable(Level.FINEST)) {
            StackTraceElement s = Thread.currentThread().getStackTrace()[2];
            impl.logp(Level.FINEST, s.getClassName(), s.getMethodName() + ":" + s.getLineNumber(), msg);
        }
    }

    public void error(String msg){
        if(impl.isLoggable(Level.FINEST)) {
            StackTraceElement s = Thread.currentThread().getStackTrace()[2];
            impl.logp(Level.SEVERE, s.getClassName(), s.getMethodName() + ":" + s.getLineNumber(), msg);
        }
    }
    public void warn(String msg){
        if(impl.isLoggable(Level.WARNING)) {
            StackTraceElement s = Thread.currentThread().getStackTrace()[2];
            impl.logp(Level.WARNING, s.getClassName(), s.getMethodName() + ":" + s.getLineNumber(), msg);
        }
    }

    public void error(Throwable e) {
        if (!impl.isLoggable(Level.SEVERE)) {
            return;
        }
        StackTraceElement s = Thread.currentThread().getStackTrace()[2];
        LogRecord lr = new LogRecord(Level.SEVERE, e.getMessage());
        lr.setSourceClassName(s.getClassName());
        lr.setSourceMethodName(s.getMethodName()+":" + s.getLineNumber());
        lr.setThrown(e);

        impl.log(lr);
    }
    public void error(String msg, Throwable e){
        if (!impl.isLoggable(Level.SEVERE)) {
            return;
        }
        StackTraceElement s = Thread.currentThread().getStackTrace()[2];
        LogRecord lr = new LogRecord(Level.SEVERE, msg);
        lr.setSourceClassName(s.getClassName());
        lr.setSourceMethodName(s.getMethodName()+":" + s.getLineNumber());
        lr.setThrown(e);
        impl.log(lr);
    }

}
