package com.persistentbit.core.logging;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 14/12/16
 */
public class LogMessageEntry extends BaseValueClass implements LogEntry{

	private final LogType type;
	private final int     lineNumber;
	private final String  message;

	public LogMessageEntry(LogType type, int lineNumber, String message) {
		this.type = type;
		this.lineNumber = lineNumber;
		this.message = message;
	}

	public LogType getType() {
		return type;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getMessage() {
		return message;
	}
}