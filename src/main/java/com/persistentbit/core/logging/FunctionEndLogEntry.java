package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public class FunctionEndLogEntry extends AbstractLogEntry{
    private final String returnValue;

    public FunctionEndLogEntry(int functionCallId, long timestamp, String className, String methodName, int lineNumber, int callStackLength, String returnValue) {
        super(functionCallId, timestamp, className, methodName, lineNumber, callStackLength);
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        return "" + functionCallId + " done: " + returnValue;
    }

    public String getReturnValue() {
        return returnValue;
    }

    @Override
    public boolean hasError() {
        return false;
    }
}
