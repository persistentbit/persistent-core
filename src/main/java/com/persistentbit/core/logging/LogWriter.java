package com.persistentbit.core.logging;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * TODOC
 *
 * @author petermuys
 * @since 16/12/16
 */
public class LogWriter implements LogEntryVisitor{

	private boolean          hasError;
	private Consumer<String> lineConsumer;
	private       String                              indent              = "";
	private final Map<Integer, FunctionStartLogEntry> functionStartLookup = new HashMap<>();

	private LogWriter(boolean hasError, Consumer<String> lineConsumer) {
		this.hasError = hasError;
		this.lineConsumer = lineConsumer;
	}

	static public void write(boolean hasError, List<LogEntry> logEntries, Consumer<String> lineConsumer) {
		LogWriter lw = new LogWriter(hasError, lineConsumer);
		logEntries.forEach(le -> le.accept(lw));
	}

	@Override
	public void visit(FunctionStartLogEntry le) {
		functionStartLookup.put(le.getFunctionCallId(), le);
		lineConsumer.accept(indent + "F - " + formatTime(le.getTimestamp()) + " " + le.getClassName() + "." + "(" + le
			.getLineNumber() + ")." + le.getMethodName() + "(" + le.getParams() + ")");
	}

	@Override
	public void visit(FunctionEndLogEntry le) {
		FunctionStartLogEntry startEntry = functionStartLookup.get(le.getFunctionCallId());
		if(startEntry == null) {
			lineConsumer.accept(indent + "  - " + formatTime(le.getTimestamp()) + " " + le
				.getClassName() + "." + "(" + le.getLineNumber() + ")." + le.getMethodName() + ": " + le
				.getReturnValue());
		}
		else {
			long time = (le.getTimestamp() - startEntry.getTimestamp());
			lineConsumer.accept(indent + "  - " + formatTime(le.getTimestamp()) + " " + le
				.getClassName() + "." + "(" + le.getLineNumber() + ")." + le.getMethodName() + "(" + startEntry
				.getParams() + ") in " + time + "ms : " + le.getReturnValue());
		}
	}

	@Override
	public void visit(FunctionThrowsLogEntry le) {

	}

	@Override
	public void visit(MessageLogEntry le) {

	}

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM hh:mm:ss.SSS");

	private String formatTime(long time) {
		return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
	}
}
