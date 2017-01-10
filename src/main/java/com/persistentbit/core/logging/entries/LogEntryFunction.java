package com.persistentbit.core.logging.entries;

import com.persistentbit.core.logging.LogContext;
import com.persistentbit.core.logging.printing.LogEntryDefaultFormatting;
import com.persistentbit.core.printing.PrintableText;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogEntryFunction extends AbstractLogEntry{
	private LogContext source;
	private LogEntry logs;
	private String        params;
	private Long		  timeStampDone;
	private String		  resultValue;

	LogEntryFunction(LogContext source, String params, LogEntry logs,Long timeStampDone,String resultValue) {
		this.source = source;
		this.params = params;
		this.logs = logs;
		this.timeStampDone = timeStampDone;
		this.resultValue = resultValue;
	}
	static public LogEntryFunction of(LogContext source){
		return new LogEntryFunction(source,null,LogEntryGroup.empty(),null,null);
	}

	@Override
	public LogEntryFunction append(LogEntry other) {
		return new LogEntryFunction(source, params, logs.append(other),timeStampDone,resultValue);
	}

	@Override
	public Optional<LogContext> getContext() {
		return Optional.ofNullable(source);
	}

	public Optional<String> getParams() {
		return Optional.ofNullable(params);
	}

	public LogEntry getLogs() {
		return logs;
	}

	public Optional<String> getResult() {
		return Optional.ofNullable(resultValue);
	}

	public Optional<Long> getTimestampDone() {
		return Optional.ofNullable(timeStampDone);
	}

	public LogEntryFunction	withTimestampDone(long timeStampDone){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}
	public LogEntryFunction withParams(String params){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}
	public LogEntryFunction withResultValue(String resultValue){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}
	public LogEntryFunction withLogs(LogEntry logs){
		return new LogEntryFunction(source,params,logs,timeStampDone,resultValue);
	}

	@Override
	protected PrintableText asPrintable(LogEntryDefaultFormatting formatting) {
		return out -> {

			String functionName = getContext().map(s -> {
				String fun = s.getMethodName();
				String clsName = s.getClassName();
				int i = clsName.lastIndexOf('.');
				if(i >=0){
					clsName = clsName.substring(i+1);
				}
				return clsName.replace('$','.') + "." + fun;
			}).orElse("unknownFunction");


			String duration = getTimestampDone().map( td ->
					getContext().map(c ->
							" " + (td - c.getTimestamp()) + "ms "
					).orElse("")
			).orElse("");
			String returnValue = getResult().map(r -> ": " + r).orElse("");
			out.println(
					formatting.functionStyle +  functionName +
							formatting.functionParamsStyle + getParams().map(p -> "(" + p + ")").orElse("(?)") +
							formatting.functionResultStyle + returnValue +
							formatting.timeStyle + "\t… " + getContext().map(s -> formatting.formatTime(s.getTimestamp()) + " ").orElse("") +
							formatting.durationStyle + duration  +
							formatting.classStyle  +  getContext().map(s -> s.getClassName() + "(" + s.getFileName() + ":" + s.getSourceLine() + ")").orElse("")
			);
			out.print(PrintableText.indent(getLogs().asPrintable(formatting.hasColor)));
		};
	}
}
