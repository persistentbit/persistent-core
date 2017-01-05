package com.persistentbit.core.logging;

import java.util.Objects;
import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 5/01/2017
 */
public class LogEntryBlock implements LogEntry{
    private final LogEntry content;

    private LogEntryBlock(LogEntry content) {
        this.content = Objects.requireNonNull(content);
    }
    static public LogEntryBlock create() {
        return new LogEntryBlock(LogEntryEmpty.inst);
    }

    @Override
    public LogEntry append(LogEntry other) {
        return new LogEntryBlock(content.append(other));
    }

    @Override
    public Optional<LogContext> getContext() {
        return content.getContext();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }
}
