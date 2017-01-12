package com.persistentbit.core.logging.entries;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogContext{
	private final String fileName;
	private final String className;
	private final String methodName;
	private final int sourceLine;
	private final long timestamp;

	public LogContext(String fileName, String className, String methodName, int sourceLine,long timestamp) {
		this.fileName = fileName;
		this.className = className;
		this.methodName = methodName;
		this.sourceLine = sourceLine;
		this.timestamp = timestamp;
	}
	public LogContext(String fileName, String className, String methodName, int sourceLine){
		this(fileName,className,methodName,sourceLine,System.currentTimeMillis());
	}

	public LogContext(StackTraceElement stackTraceElement){
		this(
			stackTraceElement.getFileName(),
			stackTraceElement.getClassName(),
			stackTraceElement.getMethodName(),
			stackTraceElement.getLineNumber());
	}
	public LogContext withTimestamp(long timestamp){
		return new LogContext(fileName,className,methodName,sourceLine,timestamp);
	}

	public String getFileName() {
		return fileName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getSourceLine() {
		return sourceLine;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public LogContext withMethodName(String methodName){
		return new LogContext(fileName,className,methodName,sourceLine,timestamp);
	}

	public String toString() {
		return getClassName() + "." + getMethodName() + "(" + getFileName() + ":" + getSourceLine() + ")";
	}
}
