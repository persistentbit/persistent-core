package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.exceptions.Try;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 13/12/2016
 */
public class LogCollector{


	static private int nextFunctionId = 1;
	private final ThreadLocal<List<LogEntry>> logs = new ThreadLocal<>();

	public <V> Logged<Try<V>> logged(Supplier<Try<V>> code) {
		List<LogEntry> existingLogs = logs.get();
		logs.set(new ArrayList<>());
		Try<V> result;
		try {
			result = code.get();
		} catch(Throwable ex) {
			result = Try.failure(ex);
		}
		List<LogEntry> collected = logs.get();
		logs.set(existingLogs);
		Logged<Try<V>> loggedResult = new Logged<>(collected, result);
		return loggedResult;
	}

	public <V> Try<V> add(Logged<Try<V>> logged){
		List<LogEntry> theseLogs = this.logs.get();
		List<LogEntry> logsToAdd = logged.getLogs();
		logsToAdd.stream()
			.map(le -> le)
			.forEachOrdered(le -> theseLogs.add(le));
		return logged.getValue();
	}

	public boolean hasError() {
		List<LogEntry> ll = logs.get();
		return ll != null && ll.stream().filter(le -> le.hasError()).findAny().isPresent();
	}

	public FLog fun(Object... params) {
		int                 callId             = createFunctionId();
		Thread              currentThread      = Thread.currentThread();
		StackTraceElement[] stackTraceElements = currentThread.getStackTrace();
		StackTraceElement   currentElement     = stackTraceElements[2];
		add(new FunctionStartLogEntry(
										 currentThread.getId(),
										 callId,
										 System.currentTimeMillis(),
										 currentElement.getClassName(),
										 currentElement.getMethodName(),
										 currentElement.getLineNumber(),
										 stackTraceElements.length,
										 paramsToString(params)
		));
		return new FLog(this, callId);
	}

	static public synchronized int createFunctionId() {
		return nextFunctionId++;
	}

	public void add(LogEntry entry) {
		List<LogEntry> ll = logs.get();
		if(ll == null) {
			ll = new ArrayList<>();
			logs.set(ll);
		}
		System.out.println(entry);
		ll.add(entry);
	}

	private String paramsToString(Object... params) {
		return PStream.from(params).toString(", ");
	}

	public ThreadLocal<List<LogEntry>> getLogs() {
		return logs;
	}
}
