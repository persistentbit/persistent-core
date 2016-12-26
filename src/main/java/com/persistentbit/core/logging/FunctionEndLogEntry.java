package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public class FunctionEndLogEntry extends AbstractLogEntry{
    private final String returnValue;

    public FunctionEndLogEntry(
            long threadId,
            int functionCallId,
            long timestamp,
            String className,
            String methodName,
            int lineNumber,
            int callStackLength,
            String returnValue
    ) {
        super(threadId,functionCallId, timestamp, className, methodName, lineNumber, callStackLength);
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

    @Override
    public void accept(LogEntryVisitor visitor) {
        visitor.visit(this);
    }

	@Override
	public LogEntry asCall(long threadId, int stackLevel) {
		return new FunctionEndLogEntry(
			threadId, functionCallId, timestamp, className, methodName, lineNumber, stackLevel, returnValue
		);
	}
}
