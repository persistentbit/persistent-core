package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public class FunctionThrowsLogEntry extends AbstractLogEntry{
    private final String exceptionMessage;
    private final String exceptionStackTrace;

    public FunctionThrowsLogEntry(int functionCallId, long timestamp, String className, String methodName, int lineNumber, int callStackLength, String exceptionMessage, String exceptionStackTrace) {
        super(functionCallId, timestamp, className, methodName, lineNumber, callStackLength);
        this.exceptionMessage = exceptionMessage;
        this.exceptionStackTrace = exceptionStackTrace;
    }

    @Override
    public String toString() {
        return functionCallId + " throws " + exceptionMessage;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getExceptionStackTrace() {
        return exceptionStackTrace;
    }

    @Override
    public boolean hasError() {
        return true;
    }
}
