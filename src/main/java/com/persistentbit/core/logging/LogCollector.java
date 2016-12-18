package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PStream;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 13/12/2016
 */
public class LogCollector {




    private final ThreadLocal<List<LogEntry>> logs = new ThreadLocal<>();

    static private int nextFunctionId = 1;

    static public synchronized int createFunctionId() {
        return nextFunctionId++;
    }

    public void add(LogEntry entry) {
        List<LogEntry> ll = logs.get();
        if (ll == null) {
            ll = new ArrayList<>();
            logs.set(ll);
        }
        System.out.println(entry);
        ll.add(entry);
    }

    public boolean hasError() {
        List<LogEntry> ll = logs.get();
        return ll != null && ll.stream().filter(le -> le.hasError()).findAny().isPresent();
    }

    public FLog fun(Object... params) {
        int callId = createFunctionId();
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stackTraceElements = currentThread.getStackTrace();
        StackTraceElement currentElement = stackTraceElements[2];
        add(new FunctionStartLogEntry(
                callId,
                System.currentTimeMillis(),
                currentElement.getClassName(),
                currentElement.getMethodName(),
                currentElement.getLineNumber(),
                stackTraceElements.length,
                paramsToString(params)
        ));
        return new FLog(this,callId);
    }

    private String paramsToString(Object...params){
        return PStream.from(params).toString(", ");
    }

    public ThreadLocal<List<LogEntry>> getLogs() {
        return logs;
    }
}
