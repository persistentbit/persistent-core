package com.persistentbit.core.logging;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryFunction implements LogEntry{
	private LogContext    source;
	private LogEntryGroup logs;
	private String        params;

	public LogEntryFunction(LogContext source, String params, LogEntryGroup logs) {
		this.source = source;
		this.params = params;
		this.logs = logs;
	}
	public LogEntryFunction(LogContext source){
		this(source,null,LogEntryGroup.empty());
	}

	@Override
	public LogEntry append(LogEntry other) {
		return new LogEntryFunction(source, params, logs.append(other));
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.ofNullable(source);
	}

	public Optional<String> getParams() {
		return Optional.ofNullable(params);
	}

	public LogEntryGroup getLogs() {
		return logs;
	}

	public Optional<LogEntryResult> getResult() {
		return logs.getEntries().find(le -> le instanceof LogEntryResult).map(ler -> (LogEntryResult)ler);
	}

	@Override
	public String toString() {
		return "fun " + source.getMethodName() + "(" + getParams().orElse("") + ")" + "{ " + getLogs() + "}";
	}
}
