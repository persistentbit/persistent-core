package com.persistentbit.core.logging;

/**
 * @author petermuys
 * @since 30/12/16
 */
public class LoggedException extends RuntimeException{
	private LogEntry logs;

	public LoggedException(LogEntry logs) {
		this.logs = logs;
	}

	public LoggedException setLogs(LogEntry logs) {
		this.logs = logs;
		return this;
	}

	@Override
	public String toString() {
		return logs.toString();
	}

	public LoggedException(String message, LogEntry logs) {
		super(message);
		this.logs = logs;
	}

	public LoggedException(String message, Throwable cause, LogEntry logs) {
		super(message, cause);
		this.logs = logs;
	}

	public LoggedException(Throwable cause, LogEntry logs) {
		super(cause.getMessage(),cause);
		/*if(cause instanceof LoggedException){
			LoggedException lc = (LoggedException)cause;
			logs = logs.append(lc.logs);
			//logs = logs.append(LogEntry.of(lc.logs.get().map(s -> "  " + s)));
			//lc.logs = LogEntryMonoid.of(lc.logs.get().head());
			//initCause(lc.getCause());
		} else {
			logs = logs.append(new LogEntryException(cause));
			//logs = logs.append("EXC " + cause.getMessage());
			initCause(cause);
		}
		//initCause(cause);*/
		this.logs = logs;
	}

	public LogEntry getLogs() {
		return logs;
	}


}
