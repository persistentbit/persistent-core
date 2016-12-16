package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public abstract class AbstractLogEntry implements LogEntry{
    protected final int functionCallId;
    protected final long timestamp;
    protected final String className;
    protected final String methodName;
    protected final int lineNumber;
    protected final int callStackLength;

    public AbstractLogEntry(int functionCallId, long timestamp, String className, String methodName, int lineNumber, int callStackLength) {
        this.functionCallId = functionCallId;
        this.timestamp = timestamp;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.callStackLength = callStackLength;
    }

    public int getFunctionCallId() {
        return functionCallId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getCallStackLength() {
        return callStackLength;
    }
}
