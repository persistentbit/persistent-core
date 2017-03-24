package com.persistentbit.core.logging.printing;

import com.persistentbit.core.logging.entries.LogEntry;

import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public class LogPrintStream implements LogPrint{

	private final LogFormatter logFormatter;
	private final Supplier<PrintStream> outSupplier;

	public LogPrintStream(LogFormatter logFormatter, Supplier<PrintStream> outSupplier) {
		this.logFormatter = logFormatter;
		this.outSupplier = outSupplier;
	}

	public static LogPrintStream sysOut(LogFormatter lp) {
		return new LogPrintStream(lp, ()->System.out);
	}

	public static LogPrintStream sysErr(LogFormatter lp) {
		return new LogPrintStream(lp, ()->System.err);
	}

	@Override
	public void print(LogEntry logEntry) {
		try {
			outSupplier.get().print(logFormatter.printableLog(logEntry).printToString());
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void print(Throwable exception) {
		try {
			outSupplier.get().print(logFormatter.printableException(exception).printToString());
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}
}