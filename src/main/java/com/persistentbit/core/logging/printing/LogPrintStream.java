package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;

import java.io.PrintStream;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public class LogPrintStream implements LogPrint{

	private final LogFormatter logFormatter;
	private final PrintStream  out;

	public LogPrintStream(LogFormatter logFormatter, PrintStream out) {
		this.logFormatter = logFormatter;
		this.out = out;
	}

	public static LogPrintStream sysOut(LogFormatter lp) {
		return new LogPrintStream(lp, System.out);
	}

	public static LogPrintStream sysErr(LogFormatter lp) {
		return new LogPrintStream(lp, System.err);
	}

	@Override
	public void print(LogEntry logEntry) {
		try {
			out.print(logFormatter.printableLog(logEntry).printToString());
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void print(Throwable exception) {
		try {
			out.print(logFormatter.printableException(exception).printToString());
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}
}