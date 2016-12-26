package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public class FunctionStartLogEntry extends AbstractLogEntry{
    private final String params;


    public FunctionStartLogEntry(
            long threadId,
            int functionCallId,
            long timestamp,
            String className,
            String methodName,
            int lineNumber,
            int callStackLength,
            String params
    ) {
        super(threadId,functionCallId, timestamp, className, methodName, lineNumber, callStackLength);
        this.params = params;
    }

    @Override
    public String toString() {
        return  functionCallId + " " + className + "." + methodName + "(" + params + ")";
    }

    public String getParams() {
        return params;
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
		return new FunctionStartLogEntry(
			threadId, functionCallId, timestamp, className, methodName, lineNumber, stackLevel, params
		);
	}
}
