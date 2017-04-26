package com.persistentbit.core.logging;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.doc.annotations.DAggregate;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.logging.entries.LogEntryEmpty;

/**
 * A collector for a {@link LogEntry}.<br>
 *
 *
 * @author Peter Muys
 * @since 13/04/2017
 */
public class LogCollector   extends AbstractLogEntryLogging {
	@DAggregate
    private LogEntry entry;

    public LogCollector() {
        super(1);
        this.entry = new LogEntryEmpty();
    }

    @Override
    public Nothing add(LogEntry logEntry) {
        this.entry = this.entry.append(logEntry);
        return Nothing.inst;
    }

    public LogEntry getEntry() {
        return entry;
    }
}
