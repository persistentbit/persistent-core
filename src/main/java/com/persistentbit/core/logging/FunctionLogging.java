package com.persistentbit.core.logging;

import com.persistentbit.core.Nothing;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/01/17
 */
public class FunctionLogging extends AbstractLogEntryLogging {

	@FunctionalInterface
	public interface LoggedFunction<R>{
		R run(FunctionLogging l) throws Exception;
	}

	protected LogEntryFunction entry;


	public FunctionLogging(LogEntryFunction entry, int stackEntryIndex) {
		super(stackEntryIndex);
		this.entry = entry;
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



	protected void map(Function<LogEntryFunction,LogEntryFunction> mapper){
		entry = mapper.apply(entry);
	}
	public void functionDoneTimestamp(long timestamp){
		map(e -> e.withTimestampDone(timestamp));
	}
	public void functionResult(Object result){
		map(e -> e.withResultValue(objectToString(result)));
	}



	public FunctionLogging params(Object...params){
		map(e -> e.withParams(objectsToString(params)));
		return this;
	}




}
