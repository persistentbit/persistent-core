package com.persistentbit.core.logging;

import com.persistentbit.core.exceptions.SpecificExceptionPrinter;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.logging.printing.LogPrinter;
import com.persistentbit.core.printing.PrintableText;

/**
 * @author petermuys
 * @since 30/12/16
 */
public class LoggedException extends RuntimeException{
	private LogEntry logs;

	public LoggedException(LogEntry logs) {
		this.logs = logs;
	}

	public LoggedException setLogs(LogEntry logs) {
		this.logs = logs;
		return this;
	}

	@Override
	public String toString() {
		return logs.toString();
	}

	public LoggedException(String message, LogEntry logs) {
		super(message);
		this.logs = logs;
	}

	public LoggedException(String message, Throwable cause, LogEntry logs) {
		super(message, cause);
		this.logs = logs;
	}

	public LoggedException(Throwable cause, LogEntry logs) {
		super(cause.getMessage(),cause);

		this.logs = logs;
	}

	public LogEntry getLogs() {
		return logs;
	}


	public static SpecificExceptionPrinter<LoggedException> createExceptionPrinter(LogPrinter logPrinter, boolean color) {
		LogEntryDefaultFormatting format =
			color ? LogEntryDefaultFormatting.colors : LogEntryDefaultFormatting.noColors;
		return (exception, rootPrinter) -> out -> {
			out.println(format.msgStyleException + "Logged Exception: " + exception.getMessage());
			out.print(PrintableText.indent(indent -> {
				indent.print(logPrinter.asPrintable(exception.getLogs()));
				for(StackTraceElement element : exception.getStackTrace()) {
					indent.println(format.classStyle + element.getClassName() + "." + element.getMethodName()
									   + "(" + element.getFileName() + ":" + element.getLineNumber() + ")"
					);
				}
				Throwable cause = exception.getCause();
				if(cause != null) {
					indent.println(format.msgStyleException + " caused by..");
					indent.println(rootPrinter.asPrintable(cause).printToString());
				}
			}));
		};
	}
}
