package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;

import java.io.PrintStream;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public class LogOutPrintStream implements LogOut{
    private final LogPrinter logPrinter;
    private final PrintStream out;

    public LogOutPrintStream(LogPrinter logPrinter, PrintStream out) {
        this.logPrinter = logPrinter;
        this.out = out;
    }

    static public LogOutPrintStream sysOut(LogPrinter lp){
        return new LogOutPrintStream(lp,System.out);
    }
    static public LogOutPrintStream sysErr(LogPrinter lp){
        return new LogOutPrintStream(lp,System.err);
    }

    @Override
    public void print(LogEntry logEntry) {
        try{
            out.print(logPrinter.printableLog(logEntry).printToString());
        }catch (Exception e){

        }
    }

    @Override
    public void print(Throwable exception) {
        try{
            out.print(logPrinter.printableException(exception).printToString());
        }catch (Exception e){

        }
    }
}