package com.persistentbit.core.logging.printing;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.entries.LogEntry;

/**
 * A {@link LogPrint} that prints to a {@link StringBuilder}
 *
 * @author petermuys
 * @since 24/06/17
 */
public class LogPrintToString implements LogPrint{
	private static final int sDefaultBufferSize = 10 * 1024;
	private final LogFormatter          logFormatter;
	private StringBuilder	output;

	public LogPrintToString(LogFormatter logFormatter) {
		this.logFormatter = logFormatter;
		this.output = new StringBuilder(sDefaultBufferSize);
	}

	public LogPrintToString() {
		this(ModuleCore.createLogFormatter());
	}


	@Override
	public void print(LogEntry logEntry) {
		doPrint(logEntry, logFormatter.printableLog(logEntry).printToString());
	}

	@Override
	public void print(Throwable exception) {
		doPrint(exception, logFormatter.printableException(exception).printToString());
	}

	private synchronized void doPrint(Object value, String msg){
		output.append(msg);
	}

	@Override
	public String toString() {
		return "LogPrintToString";
	}

	/**
	 *
	 * @return The log string
	 */
	public String getLogString() {
		return output.toString();
	}

	/**
	 * Clear the logs.
	 * @return this
	 */
	public LogPrintToString	reset() {
		output = new StringBuilder(sDefaultBufferSize);
		return this;
	}
}
