package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/12/16
 */
public class FLog extends BaseValueClass implements LogEntry{

	private final long            threadId;
	private final long            createdTimestamp;
	private final String          className;
	private final String          methodName;
	private final int             lineNumber;
	private final String          params;
	private       String          result;
	private       long            resultTimestamp;
	private       PList<LogEntry> log;

	public FLog(long threadId, long createdTimestamp, String className, String methodName, int lineNumber,
				String params, String result, long resultTimestamp, PList<LogEntry> log
	) {
		this.threadId = threadId;
		this.createdTimestamp = createdTimestamp;
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.params = params;
		this.result = result;
		this.resultTimestamp = resultTimestamp;
		this.log = log;
	}

	FLog(long threadId, long createdTimestamp, String className, String methodName, int lineNumber,
		 String params
	) {
		this(threadId, createdTimestamp, className, methodName, lineNumber, params, null, 0, PList.empty());
	}


	public LogEntry add(LogEntry other) {
		this.log = log.plus(other);
		return other;
	}

	public <T> T debug(String name, T value) {
		StackTraceElement stackTraceElement = getStackTraceElement();
		add(new LogValueEntry(LogType.debug, stackTraceElement.getLineNumber(), name, value == null ? null : value
			.toString()));
		return value;
	}

	public <T> T info(String name, T value) {
		StackTraceElement stackTraceElement = getStackTraceElement();
		add(new LogValueEntry(LogType.info, stackTraceElement.getLineNumber(), name, value == null ? null : value
			.toString()));
		return value;
	}

	public void info(Object msg) {
		StackTraceElement stackTraceElement = getStackTraceElement();
		add(new LogMessageEntry(LogType.info, stackTraceElement.getLineNumber(), msg == null ? null : msg.toString()));
	}

	public void warn(Object msg) {
		StackTraceElement stackTraceElement = getStackTraceElement();
		add(new LogMessageEntry(LogType.warning, stackTraceElement.getLineNumber(), msg == null ? null : msg
			.toString()));
	}

	private StackTraceElement getStackTraceElement() {
		return Thread.currentThread().getStackTrace()[3];
	}

	public <T> T result(T value) {
		this.resultTimestamp = System.currentTimeMillis();
		this.result = value == null ? null : value.toString();
		return value;
	}

	public void result() {
		this.resultTimestamp = System.currentTimeMillis();
		this.result = null;
	}
}
