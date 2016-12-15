package com.persistentbit.core.logging;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 14/12/16
 */
public class LogValueEntry extends BaseValueClass implements LogEntry{

	private final LogType type;
	private final int     lineNumber;
	private final String  name;
	private final String  value;

	public LogValueEntry(LogType type, int lineNumber, String name, String value) {
		this.type = type;
		this.lineNumber = lineNumber;
		this.name = name;
		this.value = value;
	}
}
