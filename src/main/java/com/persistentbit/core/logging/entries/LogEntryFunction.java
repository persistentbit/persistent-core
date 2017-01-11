package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryFunction extends AbstractLogEntry{
	private LogContext source;
	private LogEntry logs;
	private String        params;
	private Long		  timeStampDone;
	private String		  resultValue;

	LogEntryFunction(LogContext source, String params, LogEntry logs,Long timeStampDone,String resultValue) {
		this.source = source;
		this.params = params;
		this.logs = logs;
		this.timeStampDone = timeStampDone;
		this.resultValue = resultValue;
	}
	static public LogEntryFunction of(LogContext source){
		return new LogEntryFunction(source,null,LogEntryGroup.empty(),null,null);
	}

	@Override
	public LogEntryFunction append(LogEntry other) {
		return new LogEntryFunction(source, params, logs.append(other),timeStampDone,resultValue);
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.ofNullable(source);
	}

	public Optional<String> getParams() {
		return Optional.ofNullable(params);
	}

	public LogEntry getLogs() {
		return logs;
	}

	public Optional<String> getResult() {
		return Optional.ofNullable(resultValue);
	}

	public Optional<Long> getTimestampDone() {
		return Optional.ofNullable(timeStampDone);
	}

	public LogEntryFunction	withTimestampDone(long timeStampDone){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}
	public LogEntryFunction withParams(String params){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}
	public LogEntryFunction withResultValue(String resultValue){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}
	public LogEntryFunction withLogs(LogEntry logs){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}


}
