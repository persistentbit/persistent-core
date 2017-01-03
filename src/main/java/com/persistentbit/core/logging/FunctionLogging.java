package com.persistentbit.core.logging;

import com.persistentbit.core.Nothing;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/01/17
 */
public class FunctionLogging{

	@FunctionalInterface
	public interface LoggedFunction<R>{
		R run(FunctionLogging l) throws Exception;
	}

	protected LogEntryFunction entry;
	private final int stackEntryIndex;

	public FunctionLogging(LogEntryFunction entry, int stackEntryIndex) {
		this.entry = entry;
		this.stackEntryIndex = stackEntryIndex;
	}

	public LogEntryFunction getLog(){
		return this.entry;
	}
	public Nothing add(LogEntry logEntry){
		if(logEntry != null){
			map(entry -> entry.append(logEntry) );
		}
		return Nothing.inst;
	}

	public <WL extends LoggedValue> WL add(WL withLogs){
		LogEntry le = withLogs.getLog();
		if(le.isEmpty()==false){
			add(le);
		}
		return withLogs;
	}

	protected void map(Function<LogEntryFunction,LogEntryFunction> mapper){
		entry = mapper.apply(entry);
	}
	public void functionDoneTimestamp(long timestamp){
		map(e -> e.withTimestampDone(timestamp));
	}
	public void functionResult(Object result){
		map(e -> e.withResultValue(objectToString(result)));
	}

	public Nothing info(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.info, new LogContext(ste), objectToString(message)));
	}
	public Nothing info(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.info, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}
	public Nothing info(String message, Object value,Object...otherValues){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		String values = objectToString(value) + ", " + objectsToString(otherValues);
		return add(LogEntryMessage.of(LogMessageLevel.info, new LogContext(ste), objectToString(message) + ": " + values));
	}
	public Nothing warning(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.warning, new LogContext(ste), objectToString(message)));
	}
	public Nothing warning(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.warning, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}
	public Nothing error(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.error, new LogContext(ste), objectToString(message)));
	}
	public Nothing error(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.error, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}



	public FunctionLogging params(Object...params){
		map(e -> e.withParams(objectsToString(params)));
		return this;
	}

	private final String objectToString(Object message){
		if(message == null){
			return "null";
		}
		try{
			return message.toString();
		}catch(Exception e){
			return "<Message to string failed>";
		}
	}
	private final String objectsToString(Object...others){
		String res = "";
		for(Object param : others){
			if(res.isEmpty() == false){
				res += ", ";
			}
			res += objectToString(param);
		}
		return res;
	}



}
