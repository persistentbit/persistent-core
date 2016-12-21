package com.persistentbit.core.logging;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public class MessageLogEntry extends AbstractLogEntry{
    private final LogCategory category;
    private final String message;

    public MessageLogEntry(
            long threadId,
            int functionCallId,
            long timestamp,
            String className,
            String methodName,
            int lineNumber,
            int callStackLength,
            LogCategory category,
            String message
    ) {
        super(threadId,functionCallId, timestamp, className, methodName, lineNumber, callStackLength);
        this.category = category;
        this.message = message;
    }

    public LogCategory getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return functionCallId + " " + category + message;
    }

    @Override
    public boolean hasError() {
        return category == LogCategory.error;
    }

    @Override
    public void accept(LogEntryVisitor visitor) {
        visitor.visit(this);
    }
}
